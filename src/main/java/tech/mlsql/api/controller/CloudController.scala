package tech.mlsql.api.controller

import net.csdn.annotation.rest._
import net.csdn.modules.http.ApplicationController


@OpenAPIDefinition(
  info = new BasicInfo(
    desc = "Create Instance;Show Instances running;",
    state = State.alpha,
    contact = new Contact(url = "https://github.com/allwefantasy", name = "WilliamZhu", email = "allwefantasy@gmail.com"),
    license = new License(name = "Apache-2.0", url = "")),
  externalDocs = new ExternalDocumentation(description =
    """

    """),
  servers = Array()
)
class CloudController extends ApplicationController {


}
