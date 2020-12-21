package yhsb.net

import groovy.transform.TypeChecked
import java.util.Map.Entry

@TypeChecked
class HttpHeader implements Iterable<Entry<String, String>> {
	private HashMap<String, ArrayList<String>> header = new HashMap<>()

	@Override
	Iterator<Entry<String, String>> iterator() {
		header.entrySet().stream().flatMap { e ->
			e.value.stream().map {v ->
				Map.entry(e.key, v)
			}
		}.iterator()
	}

	boolean containsKey(String key) {
		header.containsKey(key.toLowerCase())
	}

	ArrayList<String> getValues(String key) {
		header[key.toLowerCase()]
	}

	void addValue(String key, String value) {
		def k = key.toLowerCase()
		if (!header.containsKey(k)) {
			header[k] = new ArrayList<String>()
		}
		header[k].add(value)
	}

	void add(HttpHeader other) {
		for (e in other) {
			addValue(e.key, e.value)
		}
	}

	void remove(String key) {
		header.remove(key.toLowerCase())
	}

	void clear() {
		header.clear()
	}
}