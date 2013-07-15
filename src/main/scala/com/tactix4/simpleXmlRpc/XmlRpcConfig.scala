package com.tactix4.simpleXmlRpc

/**
 * Created by max@tactix4.com
 * 5/21/13
 */

class XmlRpcConfig (val protocol :RPCProtocol.Value, val host: String, val port: Int,  var path: String,var headers: Map[String, String]){
  def getUrl : String = protocol.toString + "://" + host + ":" + port + path

}

object XmlRpcConfig{

  def apply(protocol: RPCProtocol.Value, host: String, port: Int,path: String,headers: Map[String, String]=Map()) = {
      new XmlRpcConfig(protocol,host,port,path,headers)
  }
}


object RPCProtocol extends Enumeration {
  implicit def stringToRpcProtocol(s:String) : RPCProtocol.Value = s.toLowerCase match{
    case "http" => RPC_HTTP
    case "https"=> RPC_HTTPS
    case x => throw new XmlRpcUnsupportedProtocolException("protocol: " + x + " not supported")
  }
  val RPC_HTTP = Value("http")
  val RPC_HTTPS = Value("https")
}