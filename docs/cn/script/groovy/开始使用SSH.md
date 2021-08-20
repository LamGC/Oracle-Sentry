## 开始使用 SSH
哨兵为脚本包装了一个 SSH 客户端，通过 SSH 客户端，脚本可以通过 SSH 连接实例并执行命令、通过 Sftp 访问文件、设置端口转发。

### 创建一个 SSH 会话
SSH 会话不止一个，一个脚本可以创建多个 SSH 会话来同时做不同的事情，也可以多个脚本创建多个会话来做不同事情，会话之间互不干扰。  
创建会话的方法很简单，只需要这么做：
```groovy
def session = instance.ssh().createSession()
```
这样就能创建一个会话了，至于 SSH 的连接认证什么的，只需要交给哨兵完成即可！

> 注意：会话不保证创建成功，如果创建失败，方法将抛出一个异常。

### 通过 SSH 执行命令
在得到 SSH 会话后，就可以开始执行命令了。  
首先需要创建一个命令执行会话（虽然本质上是一个通道）：
```groovy
def execSession = session.createExecSession("date")
```
设定后，我们还需要设置命令的标准输出和标准输入，以方便我们获得命令的输出，和向命令输入内容（比如参数）：
```groovy
// 这里如果不需要获取并处理的话，可以不设置，
// 也可以将输出设定为哨兵的标准输出，也是可以的。
execSession.setOut(System.out)
// 这里也设置为哨兵的标准输入，可以由管理员主动输入内容。
execSession.setIn(System.in)
```

最后，调用 `exec()` 方法，执行命令并等待命令运行完成即可。

如果需要执行命令后自动输入之类的异步操作呢？可以改用 `exec(true)` 进行异步执行，然后使用 `waitFor()` 等待命令执行完成即可。

命令执行完成后，除了可以检查输出内容来检查程序执行结果外，还可以通过退出代码了解，只需要调用 `exitCode()` 获取退出码即可：
```groovy
if (execSession.exitCode() == 0) {
    println "命令执行成功!"
} else {
    println "命令执行失败，退出代码不为 0（退出码：${execSession.exitCode()}）"
}
```

### 完整示例代码
```groovy
run {
    def session = instance.ssh().createSession()
    def execSession = session.createExecSession("date")
    execSession.setOut(System.out)
    execSession.setIn(System.in)
    // 同步执行，并等待命令执行结束。
    execSession.exec()
    // 除了上述的 if 判断外，也可以使用 Groovy 的字符串嵌入语法。
    println "命令执行完成，退出代码：${execSession.exitCode()}"
}
```
