package tech.mlsql.test

import tech.mlsql.quill_model.MlsqlJob

class quill_modelTest extends BaseTestSpec {
  "Null content in MlsqlJob" should "not trigger NPE during rendering" in {
    val testingJob = MlsqlJob(id = 1
      , name = "testing"
      , content = null
      , status = 1
      , mlsqlUserId =1
      , reason = null
      , createdAt = 1111111L
      , finishAt = 11111111L
      , response = null)

    val testingJobRender = testingJob.render
    assert( testingJobRender.content == "")
    assert( testingJobRender.reason == "")
    assert( testingJobRender.response == "")
  }
}
