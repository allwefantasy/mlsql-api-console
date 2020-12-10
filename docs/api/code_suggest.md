# 代码提示接口

代码提示接口复用了[脚本执行接口](http://docs.mlsql.tech/mlsql-console/api/run-script.html).

下面是核心参数说明：

| 参数 | 说明 | 示例值 |
|----|----|-----|
|executeMode|autoSuggest| |
|sql|当前编辑器里的所有内容| |
|lineNum|当前光标所在行，从1开始计数| 1 |
|columnNum| 当前光标所在列,从1开始计数| 1 |

目前接口已经支持load/select语法的提示了，而且具备非常强的跨语句提示能力。
如果无法提示的，可能会报错，用户只要关注200的返回即可。

下面是一段使用JS调用该接口的例子：

```
export default class CodeIntellegence {
     static async getSuggestList(sql,lineNum,columnNum){

    const restClient = new ActionProxy()

    const res = await restClient.post("/run/script",{
        executeMode: "autoSuggest",
        sql: sql,
        lineNum: lineNum +1,
        columnNum: columnNum,
        isDebug: false,
        queryType: "robot"
    })

    if(res && res.status === 200){
        const wordList = res.content
        return wordList
    }else {
        return []
    }
   }
}
```