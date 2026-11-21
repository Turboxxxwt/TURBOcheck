package com.student.qujian.core

import com.student.qujian.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeParserTest {

    @Test
    fun `识别菜鸟驿站货架码`() {
        val r = CodeParser.parse("【菜鸟驿站】您的包裹已到达，取件码：3-2-3012，请及时取件")
        assertEquals("3-2-3012", r?.code)
        assertEquals(Platform.CAINIAO, r?.platform)
    }

    @Test
    fun `识别京东六位码`() {
        val r = CodeParser.parse("【京东快递】您的包裹已到达，取件码 417689")
        assertEquals("417689", r?.code)
        assertEquals(Platform.JD, r?.platform)
    }

    @Test
    fun `识别拼多多字母数字码`() {
        val r = CodeParser.parse("【拼多多】您的快递取件码是 A1234，请凭此取件")
        assertEquals("A1234", r?.code)
        assertEquals(Platform.PINDUODUO, r?.platform)
    }

    @Test
    fun `识别凭码取件句式与驿站`() {
        val r = CodeParser.parse("请凭 123456 到丰巢快递柜取件")
        assertEquals("123456", r?.code)
        assertTrue(r?.station?.contains("丰巢快递柜") == true)
    }

    @Test
    fun `识别身份码`() {
        val r = CodeParser.parse("【菜鸟驿站】您的身份码为 889900，请出示")
        assertEquals("889900", r?.code)
        assertTrue(r?.isIdentityCode == true)
    }

    @Test
    fun `按包名推断平台`() {
        val r = CodeParser.parse("您的包裹到了，取件码：7788", "com.xunmeng.pinduoduo")
        assertEquals(Platform.PINDUODUO, r?.platform)
    }

    @Test
    fun `非快递语境返回空`() {
        assertNull(CodeParser.parse("您的验证码是 123456，请勿泄露"))
    }

    @Test
    fun `手机号不被误判为取件码`() {
        val r = CodeParser.parse("【快递】客服 13800138000 联系您，取件码是 5678")
        assertEquals("5678", r?.code)
    }

    @Test
    fun `货架码提取柜号`() {
        val r = CodeParser.parse("【菜鸟驿站】取件码：3-2-3012")
        assertEquals("3号柜", r?.cabinet)
    }

    @Test
    fun `文本中的号柜提取`() {
        val r = CodeParser.parse("【丰巢】您的包裹在 12号柜，取件码 556677")
        assertEquals("12号柜", r?.cabinet)
    }

    @Test
    fun `文本中的区提取`() {
        val r = CodeParser.parse("【菜鸟驿站】A区取件码 8899")
        assertEquals("A区", r?.cabinet)
    }

    @Test
    fun `无柜号时为空`() {
        val r = CodeParser.parse("【京东快递】取件码 417689")
        assertNull(r?.cabinet)
    }

    @Test
    fun `样例1 室外快递柜和5号柜`() {
        val r = CodeParser.parse("取件码 17917704\n近邻宝快递柜 | 航院近邻宝【5号柜】K1")
        assertEquals("17917704", r?.code)
        assertEquals("室外快递柜", r?.area)
        assertEquals("5号柜", r?.spot)
    }

    @Test
    fun `样例2 室内货架区和009货架`() {
        val r = CodeParser.parse("取件码 009-2-3070\n郑航近邻宝拼夕夕室内货架区 | 近邻宝多多驿站货架区")
        assertEquals("009-2-3070", r?.code)
        assertEquals("室内货架区", r?.area)
        assertEquals("009货架", r?.spot)
    }

    @Test
    fun `近邻宝书名号包裹两段码去横线并按原规则分类`() {
        val r = CodeParser.parse("【近邻宝】凭「5940-3017」到航院近邻宝【6号柜】N3取件。")
        assertEquals("59403017", r?.code)
        assertEquals("室外快递柜", r?.area)
        assertEquals("6号柜", r?.spot)
    }

    @Test
    fun `手动导入多条短信按空行拆分解析`() {
        val pasted = """
            取件码 17917704
            近邻宝快递柜 | 航院近邻宝【5号柜】K1

            取件码 009-2-3070
            郑航近邻宝拼夕夕室内货架区 | 近邻宝多多驿站货架区
        """.trimIndent()
        val blocks = pasted.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotEmpty() }
        assertEquals(2, blocks.size)
        val r1 = CodeParser.parse(blocks[0])
        val r2 = CodeParser.parse(blocks[1])
        assertEquals("17917704", r1?.code)
        assertEquals("5号柜", r1?.spot)
        assertEquals("009-2-3070", r2?.code)
        assertEquals("009货架", r2?.spot)
    }
}
