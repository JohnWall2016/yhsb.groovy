package yhsb

def testLabel() {
	int i = 0
	println 'testlabel'
	def stop = true
	while (stop) {
		println "continue $i"
		switch (i++) {
			case 0 ..< 10: println i; break
			default: stop = false; break 
		}
	}
	exit: println 'exit'
}

testLabel()