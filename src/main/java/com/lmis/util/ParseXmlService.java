package com.lmis.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by chunsoft on 15/12/3.
 */
public class ParseXmlService {
    public HashMap<String,String> parseXml(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        HashMap<String,String> hashMap = new HashMap<String,String>();
        //实例化一个文档构建器工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //通过一个文档构建器工厂获得一个文档构建器
        DocumentBuilder builder = factory.newDocumentBuilder();
        //通过文档构建器构建一个文档实例
        Document document = builder.parse(inputStream);
        //获取XML文件根节点
        Element root = document.getDocumentElement();
        //获取所有节点
        NodeList childNodes = root.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++)
        {
            //遍历子节点
            Node childNode = (Node) childNodes.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childElement = (Element) childNode;
                //版本号
                if ("versionCode".equals(childElement.getNodeName()))
                {
                    hashMap.put("version",childElement.getFirstChild().getNodeValue());
                }
                //软件名称
                else if (("appName".equals(childElement.getNodeName())))
                {
                    hashMap.put("name",childElement.getFirstChild().getNodeValue());
                }
                //下载地址
                else if (("apkUrl".equals(childElement.getNodeName())))
                {
                    hashMap.put("url",childElement.getFirstChild().getNodeValue());
                }
            }
        }
        return hashMap;

    }
}
