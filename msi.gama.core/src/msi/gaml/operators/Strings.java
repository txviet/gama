/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gaml.operators;

import java.util.*;
import java.util.regex.*;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gaml.types.IType;
import org.joda.time.*;
import org.joda.time.chrono.*;
import org.joda.time.field.PreciseDurationField;
import org.joda.time.format.*;

/**
 * Written by drogoul Modified on 10 d�c. 2010
 * 
 * @todo Description
 * 
 */
public class Strings {

	static Pattern p = Pattern.compile("%[YMDhms]");
	private static PeriodFormatterBuilder format;
	private static PeriodFormatter dateFormat;
	private static PeriodFormatter timeFormat;
	private static GamaChronology chronology;

	static PeriodFormatterBuilder getCustomFormat() {
		if ( format == null ) {
			format = new PeriodFormatterBuilder();
		}
		return format;
	}

	static PeriodFormatter getTimeFormat() {
		if ( timeFormat == null ) {
			timeFormat =
				new PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2)
					.appendHours().appendLiteral(":").appendMinutes().appendLiteral(":")
					.appendSeconds().toFormatter();
		}
		return timeFormat;
	}

	static PeriodFormatter getDateFormat() {
		if ( dateFormat == null ) {
			dateFormat =
				new PeriodFormatterBuilder().appendYears().appendSuffix(" year", " years")
					.appendSeparator(", ").appendMonths().appendSuffix(" month", " months")
					.appendSeparator(", ").appendWeeks().appendSuffix(" week", " weeks")
					.appendSeparator(", ").appendDays().appendSuffix(" day", " days")
					.appendSeparator(" ").toFormatter();
		}
		return dateFormat;
	}

	static GamaChronology getChronology() {
		if ( chronology == null ) {
			chronology = new GamaChronology(GregorianChronology.getInstanceUTC());
		}
		return chronology;
	}

	@operator(value = IKeyword.PLUS, can_be_const = true)
	@doc(special_cases = "if the left-hand operand is a string, returns the concatenation of the two operands (the left-hand one beind casted into a string")
	public static String opPlus(final String a, final String b) {
		return a + b;
	}

	@operator(value = IKeyword.PLUS, can_be_const = true)
	@doc(examples = "\"hello \" + 12 	--: 	\"hello 12\"")
	public static String opPlus(final IScope scope, final String a, final Object b)
		throws GamaRuntimeException {
		return a + Cast.asString(scope, b);
	}

	@operator(value = "in", can_be_const = true)
	@doc(special_cases = "if both operands are strings, returns true if the left-hand operand patterns is included in to the right-hand string;", examples = " 'bc' in 'abcded' 	--:  true")
	public static Boolean opIn(final String pattern, final String target) {
		return target.contains(pattern);
	}

	@operator(value = "contains", can_be_const = true)
	@doc(special_cases = "if both operands are strings, returns true if the right-hand operand contains the right-hand pattern;", examples = "'abcded' contains 'bc' 	--:  true")
	public static Boolean opContains(final String target, final String pattern) {
		return opIn(pattern, target);
	}

	@operator(value = "contains_any", can_be_const = true, expected_content_type = { IType.STRING })
	@doc(examples = "\"abcabcabc\" contains_any [\"ca\",\"xy\"] 	--: 	true")
	public static Boolean opContainsAny(final String target, final List l) {
		for ( Object o : l ) {
			if ( o instanceof String && opContains(target, (String) o) ) { return true; }
		}
		return false;
	}

	@operator(value = "contains_all", can_be_const = true, expected_content_type = { IType.STRING })
	@doc(examples = "\"abcabcabc\" contains_all [\"ca\",\"xy\"] 	--: 	false")
	public static Boolean opContainsAll(final String target, final List l) {
		for ( Object o : l ) {
			if ( !(o instanceof String && opContains(target, (String) o)) ) { return false; }
		}
		return true;
	}

	@operator(value = "index_of", can_be_const = true)
	@doc(special_cases = "if both operands are strings, returns the index within the left-hand string of the first occurrence of the given right-hand string", examples = " \"abcabcabc\" index_of \"ca\" 	--: 	2")
	public static Integer opIndexOf(final String target, final String pattern) {
		return target.indexOf(pattern);
	}

	@operator(value = "last_index_of", can_be_const = true)
	@doc(special_cases = "if both operands are strings, returns the index within the left-hand string of the rightmost occurrence of the given right-hand string", examples = "\"abcabcabc\" last_index_of \"ca\" 	--: 	5")
	public static Integer opLastIndexOf(final String target, final String pattern) {
		return target.lastIndexOf(pattern);
	}

	@operator(value = { "copy_between" /* , "copy" */}, can_be_const = true)
	@doc(examples = "\"abcabcabc\" copy_between {2,6}	--: 	cabc")
	public static String opCopy(final String target, final GamaPoint p) {
		final int beginIndex = p.x < 0 ? 0 : (int) p.x;
		final int endIndex = p.y > target.length() ? target.length() : (int) p.y;
		if ( beginIndex > endIndex ) { return ""; }
		return target.substring(beginIndex, endIndex);
	}

	@operator(value = { "split_with", "tokenize" }, can_be_const = true)
	@doc(value = "a list, containing the sub-strings (tokens) of the left-hand operand delimited by each of the characters of the right-hand operand.", comment = "delimiters themselves are excluded from the resulting list", examples = "'to be or not to be,that is the question' split_with ' ,'  --:  [to,be,or,not,to,be,that,is,the,question]")
	public static IList opTokenize(final String target, final String pattern) {
		final StringTokenizer st = new StringTokenizer(target, pattern);
		return new GamaList(Collections.list(st));
	}

	@operator(value = "is_number", can_be_const = true)
	@doc(value = "tests whether the operand represents a numerical value", comment = "Note that the symbol . should be used for a float value (a string with , will not be considered as a numeric value). "
		+ "Symbols e and E are also accepted. A hexadecimal value should begin with #.", examples = {
		"is_number(\"test\") 	--: false", "is_number(\"123.56\") 	--: true",
		"is_number(\"-1.2e5\") 	--: true", "is_number(\"1,2\") 	--: false",
		"is_number(\"#12FA\") 	--: true" })
	public static Boolean isGamaNumber(final String s) {
		// copright notice:
		// original code taken from org.apache.commons.lang.NumberUtils.isNumber(String)

		if ( s == null ) { return false; }
		final int length = s.length();
		if ( length == 0 ) { return false; }
		int sz = length;
		boolean hasExp = false;
		boolean hasDecPoint = false;
		boolean allowSigns = false;
		boolean foundDigit = false;

		// deal with any possible sign up front
		final int start = s.charAt(0) == '-' ? 1 : 0;
		if ( sz > start + 1 ) {
			if ( s.charAt(start) == '#' ) {
				int i = start + 1;
				if ( i == sz ) { return false; // str == "#"
				}
				// Checking hex (it can't be anything else)
				for ( ; i < length; i++ ) {
					final char c = s.charAt(i);
					if ( (c < '0' || c > '9') && (c < 'a' || c > 'f') && (c < 'A' || c > 'F') ) { return false; }
				}

				return true;
			}
		}

		sz--; // Don't want to loop to the last char, check it afterwords for type
				// qualifiers
		int i = start;

		// Loop to the next to last char or to the last char if we need another digit to
		// make a valid number (e.g. chars[0..5] = "1234E")
		while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
			final char c = s.charAt(i);
			if ( c >= '0' && c <= '9' ) {
				foundDigit = true;
				allowSigns = false;
			} else if ( c == '.' ) {
				if ( hasDecPoint || hasExp ) {
					// Two decimal points or dec in exponent
					return false;
				}
				hasDecPoint = true;
			} else if ( c == 'e' || c == 'E' ) {
				// We've already taken care of hex.
				if ( hasExp ) {
					// Two E's
					return false;
				}
				if ( foundDigit ) {
					hasExp = true;
					allowSigns = true;
				} else {
					return false;
				}
			} else if ( c == '-' ) {
				if ( allowSigns ) {
					allowSigns = false;
					foundDigit = false; // We need a digit after the E
				} else {
					return false;
				}
			} else {
				return false;
			}

			i++;
		}

		if ( i < length ) {
			final char c = s.charAt(i);
			if ( c >= '0' && c <= '9' ) {
				return true; // No type qualifier, OK
			} else if ( c == 'e' || c == 'E' ) { return false; // can't have an E at the last byte
			}
		}

		// allowSigns is true iff the val ends in 'E'
		// Found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
		return !allowSigns && foundDigit;
	}

	@operator(value = "reverse", can_be_const = true)
	@doc(special_cases = { "if it is a string, reverse returns a new string with caracters in the reversed order", }, examples = { "reverse ('abcd') 		--: 	'dcba';" })
	static public String reverse(final String s) {
		StringBuilder buf = new StringBuilder(s);
		buf.reverse();
		return buf.toString();
	}

	@operator(value = "empty", can_be_const = true)
	@doc(special_cases = { "if it is a string, empty returns true if the string does not contain any character, and false otherwise" }, examples = { "empty ('abced') 	--: 	false" })
	static public Boolean isEmpty(final String s) {
		return s != null && s.isEmpty();
	}

	@operator(value = "first", can_be_const = true)
	@doc(special_cases = { "if it is a string, first returns a string composed of its first character" }, examples = { "first ('abce')      	--:   'a'" })
	static public String first(final String s) {
		if ( s == null || s.isEmpty() ) { return ""; }
		return String.valueOf(s.charAt(0));
	}

	@operator(value = "last", can_be_const = true)
	@doc(special_cases = { "if it is a string, last returns a string composed of its last character, or an empty string if the operand is empty" }, examples = { "last ('abce') 		--:   'e'", })
	static public String last(final String s) {
		if ( s == null || s.isEmpty() ) { return ""; }
		return String.valueOf(s.charAt(s.length() - 1));
	}

	@operator(value = "length", can_be_const = true)
	@doc(special_cases = { "if it is a string, length returns the number of characters" }, examples = { "length ('I am an agent') 		--: 	13" })
	static public Integer length(final String s) {
		if ( s == null ) { return 0; }
		return s.length();
	}

	@operator(value = { IKeyword.AT, "@" }, can_be_const = true)
	@doc(examples = "'abcdef' at 0 	--: 'a';")
	public static String get(final String lv, final int rv) {
		return rv < lv.length() && rv >= 0 ? lv.substring(rv, rv + 1) : "";
	}

	private static final class GamaChronology extends AssembledChronology {

		private GamaChronology(final Chronology base) {
			super(base, null);
		}

		@Override
		protected void assemble(final AssembledChronology.Fields fields) {
			fields.months =
				new PreciseDurationField(DurationFieldType.months(), (long) IUnits.month * 1000);
			fields.years =
				new PreciseDurationField(DurationFieldType.years(), (long) IUnits.year * 1000);
		}

		@Override
		public Chronology withUTC() {
			return this;
		}

		@Override
		public Chronology withZone(final DateTimeZone zone) {
			return this;
		}

		@Override
		public String toString() {
			return "GAMA Chronology : 1 yr = 12 months ; 1 month = 30 days ";
		}

	}

	@operator(value = "as_date", can_be_const = true)
	@doc(value = "converts a number into a string with year, month, day, hour, minutes, second following a given pattern (right-hand operand)", comment = "Pattern should include : \"%Y %M %D %h %m %s\" for outputting years, months, days, hours, minutes, seconds", examples = "22324234 as_date \"%M m %D d %h h %m m %s seconds\" 	--: 8 m 18 d 9 h 10 m 34 seconds", see = { "as_time" })
	public static String asDate(final double time, final String pattern) {
		// Pattern should include : "%Y %M %D %h %m %s" for outputting years, months, days, hours,
		// minutes, seconds
		if ( pattern == null || pattern.isEmpty() ) { return asDate(time) + " " + asTime(time); }
		getCustomFormat().clear();
		List<String> dateList = new ArrayList();
		final Matcher m = p.matcher(pattern);
		int i = 0;
		while (m.find()) {
			String tmp = m.group();
			if ( i != m.start() ) {
				dateList.add(pattern.substring(i, m.start()));
			}
			dateList.add(tmp);
			i = m.end();
		}
		if ( i != pattern.length() ) {
			dateList.add(pattern.substring(i));
		}
		for ( i = 0; i < dateList.size(); i++ ) {
			String s = dateList.get(i);
			if ( s.charAt(0) == '%' && s.length() == 2 ) {
				Character c = s.charAt(1);
				switch (c) {
					case 'Y':
						getCustomFormat().appendYears();
						break;
					case 'M':
						getCustomFormat().appendMonths();
						break;
					case 'D':
						getCustomFormat().appendDays();
						break;
					case 'h':
						getCustomFormat().appendHours();
						break;
					case 'm':
						getCustomFormat().appendMinutes();
						break;
					case 's':
						getCustomFormat().appendSeconds();
						break;
					default:
						getCustomFormat().appendLiteral(s);
				}
			} else {
				getCustomFormat().appendLiteral(s);
			}
		}

		PeriodFormatter pf = getCustomFormat().toFormatter();
		PeriodType pt = PeriodType.yearMonthDayTime();
		return pf.print(new Period(new Duration((long) time * 1000), getChronology())
			.normalizedStandard(pt));

	}

	@operator(value = "as_date", can_be_const = true)
	@doc(special_cases = "used as an unary operator, uses a defined pattern with years, months, days", examples = "as_date(22324234) 	--: 8 months, 18 days")
	public static String asDate(final double time) {
		PeriodType pt = PeriodType.yearMonthDayTime();
		return getDateFormat().print(
			new Period(new Duration((long) time * 1000), getChronology()).normalizedStandard(pt));
	}

	@operator(value = "as_time", can_be_const = true)
	@doc(value = "converts the given number into a string with hours, minutes and seconds", comment = "as_time operator is a particular case (using a particular pattern) of the as_date operator.", examples = "as_time(22324234) 	--: 09:10:34", see = "as_date")
	public static String asTime(final double cycles) {
		PeriodType pt = PeriodType.yearMonthDayTime();
		return getTimeFormat().print(
			new Period(new Duration((long) cycles * 1000), getChronology()).normalizedStandard(pt));
	}

}
