package tech.mlsql

case class MlsqlJobRender(id: Int, name: String,
                          content: String,
                          status: String,
                          reason: String,
                          createdAt: String,
                          finishAt: String,
                          response: String)
