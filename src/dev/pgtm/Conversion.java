package dev.pgtm;

import static javax.measure.unit.NonSI.FAHRENHEIT;
import static javax.measure.unit.NonSI.FOOT;
import static javax.measure.unit.NonSI.MILE;
import static javax.measure.unit.NonSI.YARD;
import static javax.measure.unit.SI.CELSIUS;
import static javax.measure.unit.SI.CENTIMETER;
import static javax.measure.unit.SI.KELVIN;
import static javax.measure.unit.SI.KILOMETER;
import static javax.measure.unit.SI.METER;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

@SuppressWarnings("rawtypes")
public class Conversion {
	
	HashMap<String, Unit> stringUnits;
	HashMap<Unit, String> unitStrings;

	public Conversion() {
		
		stringUnits = new HashMap<String, Unit>();
		unitStrings = new HashMap<Unit, String>();
		
		stringUnits.put("F", FAHRENHEIT);
		stringUnits.put("C", CELSIUS);
		stringUnits.put("K", KELVIN);
		
		stringUnits.put("km", KILOMETER);
		stringUnits.put("mi", MILE);
		stringUnits.put("ft", FOOT);
		stringUnits.put("yd", YARD);
		stringUnits.put("m", METER);
		stringUnits.put("cm", CENTIMETER);
		
		
		for(Entry<String, Unit> entry : stringUnits.entrySet()) {
			unitStrings.put(entry.getValue(), entry.getKey());
		}
		
		
	}
	
	public Unit getUnit(String in) {
		if(this.stringUnits.containsKey(in)) {
			return this.stringUnits.get(in);
		}
		return null;
	}
	
	public String convertUnits(Unit in, Unit out, Double amount) {
		try {
			@SuppressWarnings("unchecked")
			UnitConverter c = in.getConverterTo(out);
			return(
					amount + unitStrings.get(in) + " is " +
					Math.round(c.convert(amount)) + unitStrings.get(out)
			);
		} catch (Exception e) {
			return "Couldn't convert " + unitStrings.get(in) + " to " + unitStrings.get(out);
		}
	}
	
	public String convertUnits(String in, String out, Double amount) {
		Unit uIn = getUnit(in);
		Unit uOut = getUnit(out);
		if(uIn != null && uOut != null) {
			return convertUnits(uIn, uOut, amount);
		} else {
			return "Couldn't convert " + in + " to " + out;
		}
	}
}
