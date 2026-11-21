package com.student.qujian.core

import com.student.qujian.model.Platform

/**
 * 取件码解析器。
 *
 * 设计原则：宁可漏，不可错。优先匹配带明确关键词的文案，
 * 避免把手机号、订单号误当成取件码。
 */
object CodeParser {

    data class Result(
        val code: String,
        val station: String?,
        val cabinet: String?, // 旧版单级柜号，保留兼容
        val area: String?,    // 区域类型：室外快递柜 / 室内货架区
        val spot: String?,    // 具体位置：5号柜 / 009货架
        val platform: Platform?,
        val isIdentityCode: Boolean,
        val confidence: Int // 0-100，越高越可信
    )

    /** 取件码候选字符集：字母、数字、短横线，长度 3-16 */
    private val TOKEN = "[A-Za-z0-9][A-Za-z0-9\\-]{2,15}"

    /** 取件码常见的包裹符：中英文引号、书名号、方括号、圆括号 */
    private val WRAP = "[「『\"'《\\[（(]?"
    private val WRAP_END = "[」』\"'》\\]）)]?"

    /** 高并发规则：关键词 + 码 */
    private val rules = listOf(
        // 取件码：xxxx / 取件码 xxxx / 取件码为 xxxx
        Rule(
            Regex("取件码(?:是|为|[:：])?\\s*#?\\s*($TOKEN)"),
            confidence = 95,
            identity = false
        ),
        // 凭 xxxx 取件 / 凭「xxxx」取件 / 凭取件码 xxxx 取件（近邻宝常用书名号包裹）
        Rule(
            Regex("凭(?:取件码)?\\s*$WRAP#?\\s*($TOKEN)\\s*$WRAP_END\\s*(?:到|取件|领取)"),
            confidence = 92,
            identity = false
        ),
        // 货架定位码：2-1-1305（末段才是有效码，整段也保留供展示）
        Rule(
            Regex("(\\d{1,2}-\\d{1,2}-\\d{3,4})"),
            confidence = 88,
            identity = false
        ),
        // 身份码 / 验证码
        Rule(
            Regex("(?:身份码|验证码)(?:是|为|[:：])?\\s*($TOKEN)"),
            confidence = 90,
            identity = true
        ),
        // 取件凭证 / 密码
        Rule(
            Regex("(?:取件凭证|取货密码|包裹密码)(?:是|为|[:：])?\\s*($TOKEN)"),
            confidence = 85,
            identity = false
        ),
        // 英文通知兜底：pickup code: xxxx
        Rule(
            Regex("(?:pickup|collection)\\s*code[:：]?\\s*($TOKEN)", RegexOption.IGNORE_CASE),
            confidence = 80,
            identity = false
        )
    )

    private data class Rule(
        val regex: Regex,
        val confidence: Int,
        val identity: Boolean
    )

    /**
     * 规范化取件码：近邻宝「5940-3017」这类两段数字码，实际取件码是 59403017，
     * 去掉中间的连字符。三段式货架码（如 3-2-3012）保持不变，用于分类识别。
     */
    private fun normalizeCode(code: String): String {
        val raw = code.trim().trim('-')
        val twoPart = Regex("^(\\d{3,6})-(\\d{3,6})$").find(raw)
        return twoPart?.let { it.groupValues[1] + it.groupValues[2] } ?: raw
    }

    /** 明显不是取件码的 token */
    private fun isInvalid(code: String): Boolean {
        val digits = code.filter { it.isDigit() }
        return when {
            code.length < 3 || code.length > 16 -> true
            digits.length >= 11 -> true // 手机号 / 订单号
            code.matches(Regex("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}")) -> true // 日期
            code.matches(Regex("\\d{1,2}:\\d{2}")) -> true // 时间
            code.all { it == '0' } -> true
            else -> false
        }
    }

    /** 从文案中提取驿站/柜子名称 */
    private fun extractStation(text: String): String? {
        val patterns = listOf(
            Regex("([^，。；,\\s]{2,12}(?:驿站|快递柜|取件点|代收点|服务中心))"),
            Regex("到([^，。；,\\s]{2,12})(?:取件|领取)"),
            Regex("([^，。；,\\s]{2,10}学院|[^，。；,\\s]{2,10}大学)[^，。；,\\s]{0,6}(?:驿站|点)")
        )
        for (p in patterns) {
            p.find(text)?.let { raw ->
                return raw.groupValues[1]
                    .trim()
                    .removePrefix("到")
                    .removePrefix("在")
                    .removePrefix("至")
            }
        }
        return null
    }

    /**
     * 从文本与解析出的码里提取柜号/柜组，用于按柜分类。
     * 优先级：显式「X号柜/X区」> 货架码首段（2-1-1305 中的 2）。
     * 返回 null 表示无法识别，UI 归入「其他」。
     */
    fun extractCabinet(text: String, code: String): String? {
        // 「3号柜」「12号柜」
        Regex("[0-9]{1,3}\\s*号柜").find(text)?.let { m ->
            val num = m.value.filter { it.isDigit() }
            return "${num}号柜"
        }
        // 「A区」「B区柜」
        Regex("(?<![A-Za-z0-9])([A-Za-z])\\s*区(?:柜)?").find(text)?.let { m ->
            return "${m.groupValues[1].uppercase()}区"
        }
        // 「柜号：A3」
        Regex("柜号\\s*[:：]?\\s*([0-9A-Za-z]{1,3})").find(text)?.let { m ->
            return m.groupValues[1].uppercase()
        }
        // 货架码：2-1-1305 → 首段「2」作为柜号
        Regex("^(\\d{1,2})-\\d{1,2}-\\d{3,4}$").find(code)?.let { m ->
            return "${m.groupValues[1]}号柜"
        }
        return null
    }

    /**
     * 智能两级分类：区域类型 + 具体位置。
     *
     * 规则：
     * - 含「快递柜 / 号柜 / 【X号柜】」→ 区域「室外快递柜」，位置取「X号柜」
     * - 含「室内货架区 / 货架区」或码为货架码 → 区域「室内货架区」，
     *   位置取货架码首段 + 「货架」（如 009-2-3070 → 009货架）
     * - 都不匹配 → ("其他", null)
     *
     * 判断区域时以文本关键词优先；位置优先取文本里显式的 X号柜 / 货架码首段。
     */
    fun extractAreaSpot(text: String, code: String): Pair<String, String?> {
        val isShelf = text.contains("室内货架") || text.contains("货架区")
        val isCabinet = text.contains("快递柜") || Regex("[0-9]{1,3}\\s*号柜").containsMatchIn(text)
        val shelfCode = Regex("^(\\d{1,3})-\\d{1,2}-\\d{3,4}$").find(code)

        // 区域判断：室内货架区优先于快递柜（示例2 同时含「货架区」和码是货架码）
        val area = when {
            isShelf || shelfCode != null -> "室内货架区"
            isCabinet -> "室外快递柜"
            else -> "其他"
        }

        val spot = when (area) {
            "室内货架区" -> shelfCode?.groupValues?.get(1)
                ?.let { "${it}货架" }
                ?: Regex("([0-9]{2,4})\\s*货架").find(text)?.let { "${it.groupValues[1]}货架" }
            "室外快递柜" -> Regex("([0-9]{1,3})\\s*号柜").find(text)
                ?.let { "${it.groupValues[1]}号柜" }
                ?: Regex("([A-Za-z][0-9]?)\\s*柜").find(text)?.let { "${it.groupValues[1].uppercase()}柜" }
            else -> null
        }
        return area to spot
    }

    /**
     * 解析一段通知/短信文本。
     * @param text 通知正文（标题 + 内容拼起来）
     * @param packageName 来源包名，可为空
     * @return 解析结果，无可用取件码时返回 null
     */
    fun parse(text: String, packageName: String? = null): Result? {
        if (text.isBlank()) return null

        // 必须出现在快递语境里，避免误抓普通验证码
        val hasContext = text.contains(Regex("取件|快递|驿站|包裹|配送|物流|丰巢|柜"))
        if (!hasContext) return null

        var best: Result? = null
        for (rule in rules) {
            val match = rule.regex.find(text) ?: continue
            val code = normalizeCode(match.groupValues[1])
            if (isInvalid(code)) continue
            val (area, spot) = extractAreaSpot(text, code)
            val candidate = Result(
                code = code,
                station = extractStation(text),
                cabinet = extractCabinet(text, code),
                area = area,
                spot = spot,
                platform = Platform.fromPackage(packageName)
                    .takeIf { it != Platform.OTHER }
                    ?: Platform.fromText(text)
                    ?: Platform.OTHER,
                isIdentityCode = rule.identity,
                confidence = rule.confidence
            )
            if (best == null || candidate.confidence > best.confidence) {
                best = candidate
            }
        }
        return best
    }
}
