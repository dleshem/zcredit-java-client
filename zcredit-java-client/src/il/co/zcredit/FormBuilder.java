package il.co.zcredit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class FormBuilder {
	private final Map<String, String> form;
	
	public static FormBuilder extract(Document doc) {
		final FormBuilder builder = new FormBuilder();
		
		// Extract input fields
		for (Element input : doc.select("input")) {
			builder.put(input.attr("name"), input.attr("value"));
		}
		
		// Extract select fields
		for (Element select : doc.select("select")) {
			String value = null;
			for (Element option : select.select("option")) {
				if ("selected".equals(option.attr("selected"))) {
					value = option.attr("value");
					break;
				}
			}
			builder.put(select.attr("name"), value);
		}
		
		return builder;
	}
	
	public FormBuilder(Map<String, String> form) {
		this.form = form;
	}
	
	public FormBuilder() {
		this(new LinkedHashMap<String, String>());
	}
	
	public FormBuilder put(String name, String value) {
		form.put(name, ((value != null) ? value : ""));
		return this;
	}
	
	public FormBuilder put(String name, Object value) {
		form.put(name, ((value != null) ? value.toString() : ""));
		return this;
	}
	
	public Map<String, String> build() {
		return form;
	}
}
