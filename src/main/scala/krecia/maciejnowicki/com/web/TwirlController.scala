package krecia.maciejnowicki.com.web

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.model.MediaTypes.{`text/html`, `text/plain`, `text/xml`}
import play.twirl.api.{Html, Txt, Xml}

abstract class TwirlController(controllerDeps: ControllerDeps) extends Controller(controllerDeps) {

  protected implicit val twirlHtmlMarshaller: ToEntityMarshaller[Html] = twirlMarshaller[Html](`text/html`)
  protected implicit val twirlTxtMarshaller: ToEntityMarshaller[Txt] = twirlMarshaller[Txt](`text/plain`)
  protected implicit val twirlXmlMarshaller: ToEntityMarshaller[Xml] = twirlMarshaller[Xml](`text/xml`)

  protected def twirlMarshaller[A <: AnyRef : Manifest](contentType: MediaType): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(contentType)(_.toString)

}
