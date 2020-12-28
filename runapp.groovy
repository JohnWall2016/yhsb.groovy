// groovy runapp.groovy task task ... --- arg arg ...

//println args

def isTask = true
def (tasks, args) = args.split {
    if (it == '--') isTask = false
    isTask
}

def cmd
if (System.getProperty("os.name").startsWith('Windows')) {
    cmd = ['./gradlew.bat', '-q', *tasks, "--args=${args.drop(1).join(' ')}"]
} else {
    cmd = ['./gradlew', '-q', *tasks, "--args=${args.drop(1).join(' ')}"]
}
// println cmd

cmd.execute().with {
    waitForProcessOutput(System.out, System.err)
}
