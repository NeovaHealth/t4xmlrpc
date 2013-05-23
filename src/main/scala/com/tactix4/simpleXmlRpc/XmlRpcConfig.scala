package com.tactix4.simpleXmlRpc

/**
 * Created by max@tactix4.com
 * 5/21/13
 */

class XmlRpcConfig (val protocol :RPCProtocol.Value, val host: String, val port: Int,  val path: String,val headers: Map[String, String]=Map()){
  require(path.startsWith("/"))
  require(port > 0 && port < 65535)

  def getUrl : String = protocol + "://" + host + ":" + port + path

}

object XmlRpcConfig{
  def apply(protocol :RPCProtocol.Value, host: String, port: Int,  path: String,headers: Map[String, String]=Map()) = {
    new XmlRpcConfig(protocol,host,port,path,headers)
  }
}


object RPCProtocol extends Enumeration {
  val RPC_HTTP = Value("http")
  val RPC_HTTPS = Value("https")
}