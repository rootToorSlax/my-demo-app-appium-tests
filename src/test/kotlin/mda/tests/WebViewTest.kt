package mda.tests

import mda.copy.Copy
import mda.harness.BaseTest
import mda.pages.HostCheck
import mda.pages.WebPage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WebViewTest : BaseTest() {
    @Test
    fun `a url without https shows the https required copy`() {
        val web = WebPage(driver)
        web.open()
        web.submitNonHttps()
        assertEquals(Copy.web.httpsRequired, web.httpsError())
    }

    @Test
    fun `a live https url is accepted by the host and the screen shows no error panel`() {
        val web = WebPage(driver)
        val status = HostCheck.status(web.liveHttpsUrl())
        assertTrue(status in 200..299) { "The host failed, not the screen. Status was $status." }
        web.open()
        web.submitLiveHttps()
        assertFalse(web.errorPanelShown())
        val title = web.titleFromWebContext()
        if (title != null) {
            assertTrue(title.isNotBlank())
        }
    }
}
