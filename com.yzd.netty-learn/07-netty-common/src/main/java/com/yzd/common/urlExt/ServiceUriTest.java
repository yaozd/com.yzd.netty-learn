package com.yzd.common.urlExt;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class ServiceUriTest {
    @Test
    public void newUriBuilder() throws MalformedURLException {
        String k8sService="/dohko/nginx?portName=test-1_2";
        //URI uri = UriUtil.newURI(k8sService);
        //URL url=new URL(k8sService);
        UriBuilder uriBuilder = UriUtil.newUriBuilder(k8sService);
        log.info("start");
    }
    @Test
    public void deleteWhitespaceTest(){
        String k8sService="/dohko/nginx?    portName=test-1_2 ";
        k8sService=null;
        String newString = StringUtils.deleteWhitespace(k8sService);
        if (newString==null) {
            log.info("newString is null");
        }
        log.info(newString);
        UriBuilder uriBuilder = UriUtil.newUriBuilder(k8sService);
        log.info("start");
    }
}
