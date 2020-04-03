package com.browserup.bup

import com.browserup.bup.mitmproxy.MitmProxyManager
import com.browserup.harreader.model.Har
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.junit.After
import org.junit.Assert
import org.junit.Test

import static org.junit.Assert.assertTrue;

class MitmProxyManagerTest {
    def mitmProxyManager = MitmProxyManager.getInstance()

    @After
    void setUp() {
        mitmProxyManager.stop()
    }

    @Test
    void proxyStartedAndHarIsAvailable() {
        //GIVEN
        def proxyPort = 8443
        mitmProxyManager.start(proxyPort)

        //WHEN
        def http = new HTTPBuilder("http://petclinic.targets.browserup.com/")
        http.setProxy("localhost", proxyPort, "http");
        http.request(Method.GET) {
            uri.path = '/'
            response.failure = { resp, json ->
                throw new AssertionError("Expected to get successful response, got: ${resp.status}")
            }
        }

        //THEN
        Har har = mitmProxyManager.getHar()
        Assert.assertNotNull(har)
        assertTrue("Expected to capture 1 har entry", har.log.entries.size() == 1)
    }



    @Test
    void stop() {
        //GIVEN
        def proxyPort = 8443
        mitmProxyManager.start(proxyPort)

        //WHEN
        def http = new HTTPBuilder("http://petclinic.targets.browserup.com/")
        http.setProxy("localhost", proxyPort, "http")
        http.request(Method.GET) {
            uri.path = '/'
            response.failure = { resp, json ->
                throw new AssertionError("Expected to get successful response, got: ${resp.status}")
            }
        }

        mitmProxyManager.stop()
        http = new HTTPBuilder("http://petclinic.targets.browserup.com/")
        http.setProxy("localhost", proxyPort, "http")
        def ex = null
        try {
            http.request(Method.GET) {
                uri.path = '/'
            }
        } catch (e) {
            ex = e
        }
        Assert.assertNotNull(ex)
        assertTrue(ex.message.contains('Connection refused'))
    }
}