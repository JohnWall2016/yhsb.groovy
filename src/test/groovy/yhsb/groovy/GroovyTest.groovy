package yhsb.groovy

import groovy.transform.TypeChecked

/*
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
*/
/*
trait SayHello {
	abstract String getName()
	void sayHello() {
		println "Hello, $name"
	}
	abstract String getCbState()
}

class Person implements SayHello {
	String name
	String cbState
}

def p = new Person(name: "John")
p.sayHello()
 */

/*
class Person {
	//private String name
	protected String name

	String name() {
		{ name }.call()
	}

	Person(String name) {
		this.name = name
	}
}

class Man extends Person {
	Man(String name) { super(name) }
}

def m = new Man('Peter')
println m.name()
*/

/*
class FakePerson {
	int age

	Object get(String fieldName) {
		if (fieldName == 'name')
			return 'John'
		else
			'Unknown'
	}

	void set(String fieldName, Object value) {
		println "set($fieldName, $value)"
	}
}

def fp = new FakePerson()
println fp.name
println fp.other
fp.name = 'Peter'
println fp.name
println fp.age
fp.age = 11
println fp.age
*/