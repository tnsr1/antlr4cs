/*
 * Copyright (c) 2012 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD-3-Clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.test.tool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
	 cover these cases:
	    dead end
	    single alt
	    single alt + preds
	    conflict
	    conflict + preds

 */
public class TestFullContextParsing extends BaseTest {
	@Test public void testAmbigYieldsCtxSensitiveDFA() {
		String grammar =
			"grammar T;\n"+
			"s" +
			"@init {_interp.setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);}\n" +
			"@after {dumpDFA();}\n" +
			"    : ID | ID {;} ;\n" +
			"ID : 'a'..'z'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		String result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
								   "abc", true);
		String expecting =
			"Decision 0:\n" +
			"s0-ID->:s1=>1\n"; // ctx sensitive
		assertEquals(expecting, result);
		assertEquals("line 1:0 reportAmbiguity d=0 (s): ambigAlts={1, 2}, input='abc'\n",
					 this.stderrDuringParse);
	}

	public String testCtxSensitiveWithoutDFA(String input) {
		String grammar =
			"grammar T;\n"+
			"s @after {dumpDFA();}\n" +
			"  : '$' a | '@' b ;\n" +
			"a : e ID ;\n" +
			"b : e INT ID ;\n" +
			"e : INT | ;\n" +
			"ID : 'a'..'z'+ ;\n"+
			"INT : '0'..'9'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		return execParser("T.g4", grammar, "TParser", "TLexer", "s", input, true);
	}

	@Test
	public void  testCtxSensitiveWithoutDFA1() {
		String result = testCtxSensitiveWithoutDFA("$ 34 abc");
		String expecting =
			"Decision 1:\n" +
			"s0-INT->s1\n" +
			"s1-ID->:s2=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:5 reportAttemptingFullContext d=1 (e), input='34abc'\n" +
					 "line 1:2 reportContextSensitivity d=1 (e), input='34'\n",
					 this.stderrDuringParse);
	}

	@Test
	public void  testCtxSensitiveWithoutDFA2() {
		String result = testCtxSensitiveWithoutDFA("@ 34 abc");
		String expecting =
			"Decision 1:\n" +
			"s0-INT->s1\n" +
			"s1-ID->:s2=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:5 reportAttemptingFullContext d=1 (e), input='34abc'\n" +
					 "line 1:5 reportContextSensitivity d=1 (e), input='34abc'\n",
					 this.stderrDuringParse);
	}

	public String testCtxSensitiveWithDFA(String input) {
		String grammar =
			"grammar T;\n"+
			"s @init{getInterpreter().enable_global_context_dfa = true;} @after {dumpDFA();}\n" +
			"  : '$' a | '@' b ;\n" +
			"a : e ID ;\n" +
			"b : e INT ID ;\n" +
			"e : INT | ;\n" +
			"ID : 'a'..'z'+ ;\n"+
			"INT : '0'..'9'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		return execParser("T.g4", grammar, "TParser", "TLexer", "s",
								   input, true);
	}

	@Test
	public void  testCtxSensitiveWithDFA1() {
		String result = testCtxSensitiveWithDFA("$ 34 abc");
		String expecting =
			"Decision 1:\n" +
			"s0-INT->s1\n" +
			"s1-ID->:s2=>1\n" +
			"s3**-ctx:15(a)->s4\n" +
			"s4-INT->:s5=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:5 reportAttemptingFullContext d=1 (e), input='34abc'\n" +
					 "line 1:2 reportContextSensitivity d=1 (e), input='34'\n",
					 this.stderrDuringParse);
	}

	@Test
	public void  testCtxSensitiveWithDFA2() {
		String result = testCtxSensitiveWithDFA("@ 34 abc");
		String expecting =
			"Decision 1:\n" +
			"s0-INT->s1\n" +
			"s1-ID->:s2=>1\n" +
			"s3**-ctx:18(b)->s4\n" +
			"s4-INT->s5\n" +
			"s5-ID->:s6=>2\n";
		assertEquals(expecting, result);
		assertEquals("line 1:5 reportAttemptingFullContext d=1 (e), input='34abc'\n" +
					 "line 1:5 reportContextSensitivity d=1 (e), input='34abc'\n",
					 this.stderrDuringParse);
	}

	@Test public void testCtxSensitiveDFATwoDiffInputWithoutDFA() {
		String grammar =
			"grammar T;\n"+
			"s @after {dumpDFA();}\n" +
			"  : ('$' a | '@' b)+ ;\n" +
			"a : e ID ;\n" +
			"b : e INT ID ;\n" +
			"e : INT | ;\n" +
			"ID : 'a'..'z'+ ;\n"+
			"INT : '0'..'9'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		String result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
								   "$ 34 abc @ 34 abc", true);
		String expecting =
			"Decision 2:\n" +
			"s0-INT->s1\n" +
			"s1-ID->:s2=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:5 reportAttemptingFullContext d=2 (e), input='34abc'\n" +
					 "line 1:2 reportContextSensitivity d=2 (e), input='34'\n" +
					 "line 1:14 reportAttemptingFullContext d=2 (e), input='34abc'\n" +
					 "line 1:14 reportContextSensitivity d=2 (e), input='34abc'\n",
					 this.stderrDuringParse);
	}

	@Test public void testCtxSensitiveDFATwoDiffInputWithDFA() {
		String grammar =
			"grammar T;\n"+
			"s @init{getInterpreter().enable_global_context_dfa = true;} @after {dumpDFA();}\n" +
			"  : ('$' a | '@' b)+ ;\n" +
			"a : e ID ;\n" +
			"b : e INT ID ;\n" +
			"e : INT | ;\n" +
			"ID : 'a'..'z'+ ;\n"+
			"INT : '0'..'9'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		String result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
								   "$ 34 abc @ 34 abc", true);
		String expecting =
			"Decision 2:\n" +
			"s0-INT->s1\n" +
			"s1-ID->:s2=>1\n" +
			"s3**-ctx:17(a)->s4\n" +
			"s3**-ctx:20(b)->s6\n" +
			"s4-INT->:s5=>1\n" +
			"s6-INT->s7\n" +
			"s7-ID->:s8=>2\n";
		assertEquals(expecting, result);
		assertEquals("line 1:5 reportAttemptingFullContext d=2 (e), input='34abc'\n" +
					 "line 1:2 reportContextSensitivity d=2 (e), input='34'\n" +
					 "line 1:14 reportAttemptingFullContext d=2 (e), input='34abc'\n" +
					 "line 1:14 reportContextSensitivity d=2 (e), input='34abc'\n",
					 this.stderrDuringParse);
	}

	@Test
	public void testSLLSeesEOFInLLGrammarWithoutDFA() {
		String grammar =
			"grammar T;\n"+
			"s @after {dumpDFA();}\n" +
			"  : a ;\n" +
			"a : e ID ;\n" +
			"b : e INT ID ;\n" +
			"e : INT | ;\n" +
			"ID : 'a'..'z'+ ;\n"+
			"INT : '0'..'9'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		String result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
								   "34 abc", true);
		String expecting =
			"Decision 0:\n" +
			"s0-INT->s1\n" +
			"s1-ID->:s2=>1\n"; // Must point at accept state
		assertEquals(expecting, result);
		assertEquals("line 1:3 reportAttemptingFullContext d=0 (e), input='34abc'\n" +
					 "line 1:0 reportContextSensitivity d=0 (e), input='34'\n",
					 this.stderrDuringParse);
	}

	@Test
	public void testSLLSeesEOFInLLGrammarWithDFA() {
		String grammar =
			"grammar T;\n"+
			"s @init{getInterpreter().enable_global_context_dfa = true;} @after {dumpDFA();}\n" +
			"  : a ;\n" +
			"a : e ID ;\n" +
			"b : e INT ID ;\n" +
			"e : INT | ;\n" +
			"ID : 'a'..'z'+ ;\n"+
			"INT : '0'..'9'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		String result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
								   "34 abc", true);
		String expecting =
			"Decision 0:\n" +
			"s0-INT->s1\n" +
			"s1-ID->:s2=>1\n" +
			"s3**-ctx:11(a)->s4\n" +
			"s4-INT->:s5=>1\n"; // Must point at accept state
		assertEquals(expecting, result);
		assertEquals("line 1:3 reportAttemptingFullContext d=0 (e), input='34abc'\n" +
					 "line 1:0 reportContextSensitivity d=0 (e), input='34'\n",
					 this.stderrDuringParse);
	}

	@Test public void testFullContextIF_THEN_ELSEParseWithoutDFA() {
		String grammar =
			"grammar T;\n"+
			"s" +
			"@init {_interp.setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);}\n" +
			"@after {dumpDFA();}\n" +
			"    : '{' stat* '}'" +
			"    ;\n" +
			"stat: 'if' ID 'then' stat ('else' ID)?\n" +
			"    | 'return'\n" +
			"    ;" +
			"ID : 'a'..'z'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		String input = "{ if x then return }";
		String result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
								   input, true);
		String expecting =
			"Decision 1:\n" +
			"s0-'}'->:s1=>2\n";
		assertEquals(expecting, result);
		assertEquals(null, this.stderrDuringParse);

		input = "{ if x then return else foo }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
			"Decision 1:\n" +
			"s0-'else'->:s1=>1\n";
		assertEquals(expecting, result);
		// Technically, this input sequence is not ambiguous because else
		// uniquely predicts going into the optional subrule. else cannot
		// be matched by exiting stat since that would only match '}' or
		// the start of a stat. But, we are using the theory that
		// SLL(1)=LL(1) and so we are avoiding full context parsing
		// by declaring all else clause parsing to be ambiguous.
		assertEquals("line 1:19 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:19 reportContextSensitivity d=1 (stat), input='else'\n",
					 this.stderrDuringParse);

		input =
			"{ if x then if y then return else foo }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
			"Decision 1:\n" +
			"s0-'}'->:s2=>2\n" +
			"s0-'else'->:s1=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:29 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:38 reportAmbiguity d=1 (stat): ambigAlts={1, 2}, input='elsefoo}'\n",
					 this.stderrDuringParse);

		// should not be ambiguous because the second 'else bar' clearly
		// indicates that the first else should match to the innermost if.
		// LL_EXACT_AMBIG_DETECTION makes us keep going to resolve

		input =
			"{ if x then if y then return else foo else bar }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
			"Decision 1:\n" +
			"s0-'else'->:s1=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:29 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:38 reportContextSensitivity d=1 (stat), input='elsefooelse'\n" +
					 "line 1:38 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:38 reportContextSensitivity d=1 (stat), input='else'\n",
					 this.stderrDuringParse);

		input =
			"{ if x then return else foo\n" +
			"if x then if y then return else foo }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
			"Decision 1:\n" +
			"s0-'}'->:s2=>2\n" +
			"s0-'else'->:s1=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:19 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:19 reportContextSensitivity d=1 (stat), input='else'\n" +
					 "line 2:27 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 2:36 reportAmbiguity d=1 (stat): ambigAlts={1, 2}, input='elsefoo}'\n",
					 this.stderrDuringParse);

		input =
			"{ if x then return else foo\n" +
			"if x then if y then return else foo }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
				"Decision 1:\n" +
				"s0-'}'->:s2=>2\n" +
				"s0-'else'->:s1=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:19 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:19 reportContextSensitivity d=1 (stat), input='else'\n" +
					 "line 2:27 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 2:36 reportAmbiguity d=1 (stat): ambigAlts={1, 2}, input='elsefoo}'\n",
					 this.stderrDuringParse);
	}

	@Test public void testFullContextIF_THEN_ELSEParseWithDFA() {
		String grammar =
			"grammar T;\n"+
			"s" +
			"@init {getInterpreter().enable_global_context_dfa = true; _interp.setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);}\n" +
			"@after {dumpDFA();}\n" +
			"    : '{' stat* '}'" +
			"    ;\n" +
			"stat: 'if' ID 'then' stat ('else' ID)?\n" +
			"    | 'return'\n" +
			"    ;" +
			"ID : 'a'..'z'+ ;\n"+
			"WS : (' '|'\\t'|'\\n')+ -> skip ;\n";
		String input = "{ if x then return }";
		String result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
								   input, true);
		String expecting =
			"Decision 1:\n" +
			"s0-'}'->:s1=>2\n";
		assertEquals(expecting, result);
		assertEquals(null, this.stderrDuringParse);

		input = "{ if x then return else foo }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
			"Decision 1:\n" +
			"s0-'else'->:s1=>1\n" +
			"s2**-ctx:7(s)->s3\n" +
			"s3-'else'->:s4=>1\n";
		assertEquals(expecting, result);
		// Technically, this input sequence is not ambiguous because else
		// uniquely predicts going into the optional subrule. else cannot
		// be matched by exiting stat since that would only match '}' or
		// the start of a stat. But, we are using the theory that
		// SLL(1)=LL(1) and so we are avoiding full context parsing
		// by declaring all else clause parsing to be ambiguous.
		assertEquals("line 1:19 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:19 reportContextSensitivity d=1 (stat), input='else'\n",
					 this.stderrDuringParse);

		input =
			"{ if x then if y then return else foo }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
			"Decision 1:\n" +
			"s0-'}'->:s8=>2\n" +
			"s0-'else'->:s1=>1\n" +
			"s2**-ctx:19(stat)->s3**\n" +
			"s3**-ctx:7(s)->s4\n" +
			"s4-'else'->s5\n" +
			"s5-ID->:s6=>1\n" +
			":s6=>1-'}'->:s7=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:29 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:38 reportAmbiguity d=1 (stat): ambigAlts={1, 2}, input='elsefoo}'\n",
					 this.stderrDuringParse);

		// should not be ambiguous because the second 'else bar' clearly
		// indicates that the first else should match to the innermost if.
		// LL_EXACT_AMBIG_DETECTION makes us keep going to resolve

		input =
			"{ if x then if y then return else foo else bar }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
			"Decision 1:\n" +
			"s0-'else'->:s1=>1\n" +
			"s2**-ctx:7(s)->s8\n" +
			"s2**-ctx:19(stat)->s3**\n" +
			"s3**-ctx:7(s)->s4\n" +
			"s4-'else'->s5\n" +
			"s5-ID->:s6=>1\n" +
			":s6=>1-'else'->:s7=>1\n" +
			"s8-'else'->:s7=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:29 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:38 reportContextSensitivity d=1 (stat), input='elsefooelse'\n" +
					 "line 1:38 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:38 reportContextSensitivity d=1 (stat), input='else'\n",
					 this.stderrDuringParse);

		input =
			"{ if x then return else foo\n" +
			"if x then if y then return else foo }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
			"Decision 1:\n" +
			"s0-'}'->:s10=>2\n" +
			"s0-'else'->:s1=>1\n" +
			"s2**-ctx:7(s)->s3\n" +
			"s2**-ctx:19(stat)->s5**\n" +
			"s3-'else'->:s4=>1\n" +
			"s5**-ctx:7(s)->s6\n" +
			"s6-'else'->s7\n" +
			"s7-ID->:s8=>1\n" +
			":s8=>1-'}'->:s9=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:19 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:19 reportContextSensitivity d=1 (stat), input='else'\n" +
					 "line 2:27 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 2:36 reportAmbiguity d=1 (stat): ambigAlts={1, 2}, input='elsefoo}'\n",
					 this.stderrDuringParse);

		input =
			"{ if x then return else foo\n" +
			"if x then if y then return else foo }";
		result = execParser("T.g4", grammar, "TParser", "TLexer", "s",
							input, true);
		expecting =
				"Decision 1:\n" +
				"s0-'}'->:s10=>2\n" +
				"s0-'else'->:s1=>1\n" +
				"s2**-ctx:7(s)->s3\n" +
				"s2**-ctx:19(stat)->s5**\n" +
				"s3-'else'->:s4=>1\n" +
				"s5**-ctx:7(s)->s6\n" +
				"s6-'else'->s7\n" +
				"s7-ID->:s8=>1\n" +
				":s8=>1-'}'->:s9=>1\n";
		assertEquals(expecting, result);
		assertEquals("line 1:19 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 1:19 reportContextSensitivity d=1 (stat), input='else'\n" +
					 "line 2:27 reportAttemptingFullContext d=1 (stat), input='else'\n" +
					 "line 2:36 reportAmbiguity d=1 (stat): ambigAlts={1, 2}, input='elsefoo}'\n",
					 this.stderrDuringParse);
	}

	/**
	 *  Tests predictions for the following case involving closures.
	 *  http://www.antlr.org/wiki/display/~admin/2011/12/29/Flaw+in+ANTLR+v3+LL(*)+analysis+algorithm
	 */
	@Test
	public void testLoopsSimulateTailRecursion() throws Exception {
		String grammar =
			"grammar T;\n" +
			"prog\n" +
			"@init {_interp.setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);}\n" +
			"    : expr_or_assign*;\n" +
			"expr_or_assign\n" +
			"    :   expr '++' {System.out.println(\"fail.\");}\n" +
			"    |   expr {System.out.println(\"pass: \"+$expr.text);}\n" +
			"    ;\n" +
			"expr: expr_primary ('<-' ID)? ;\n" +
			"expr_primary\n" +
			"    : '(' ID ')'\n" +
			"    | ID '(' ID ')'\n" +
			"    | ID\n" +
			"    ;\n" +
			"ID  : [a-z]+ ;\n" +
			"";

		String found = execParser("T.g4", grammar, "TParser", "TLexer", "prog", "a(i)<-x", true);
		assertEquals("pass: a(i)<-x\n", found);

		String expecting =
			"line 1:3 reportAttemptingFullContext d=3 (expr_primary), input='a(i)'\n" +
			"line 1:7 reportAmbiguity d=3 (expr_primary): ambigAlts={2, 3}, input='a(i)<-x'\n";
		assertEquals(expecting, this.stderrDuringParse);
	}

	@Test
	public void testAmbiguityNoLoop() throws Exception {
		// simpler version of testLoopsSimulateTailRecursion, no loops
		String grammar =
			"grammar T;\n" +
			"prog\n" +
			"@init {_interp.setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);}\n" +
			"    : expr expr {System.out.println(\"alt 1\");}\n" +
			"    | expr\n" +
			"    ;\n" +
			"expr: '@'\n" +
			"    | ID '@'\n" +
			"    | ID\n" +
			"    ;\n" +
			"ID  : [a-z]+ ;\n" +
			"WS  : [ \\r\\n\\t]+ -> skip ;\n";

		String found = execParser("T.g4", grammar, "TParser", "TLexer", "prog", "a@", true);
		assertEquals("alt 1\n", found);

		String expecting =
			"line 1:1 reportAttemptingFullContext d=0 (prog), input='a@'\n" +
			"line 1:2 reportAmbiguity d=0 (prog): ambigAlts={1, 2}, input='a@'\n" +
			"line 1:1 reportAttemptingFullContext d=1 (expr), input='a@'\n" +
			"line 1:2 reportContextSensitivity d=1 (expr), input='a@'\n";
		assertEquals(expecting, this.stderrDuringParse);
	}

	@Test
	public void testExprAmbiguity() throws Exception {
		// translated left-recursive expr rule to test ambig detection
		String grammar =
			"grammar T;\n" +
			"s\n" +
			"@init {_interp.setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);}\n" +
			"    :   expr[0] {System.out.println($expr.ctx.toStringTree(this));} ;\n" +
			"\n" +
			"expr[int _p]\n" +
			"    :   ID\n" +
			"        ( {5 >= $_p}? '*' expr[6]\n" +
			"        | {4 >= $_p}? '+' expr[5]\n" +
			"        )*\n" +
			"    ;\n" +
			"\n" +
			"ID  :   [a-zA-Z]+ ;      // match identifiers\n" +
			"WS  :   [ \\t\\r\\n]+ -> skip ; // toss out whitespace\n";

		String found = execParser("T.g4", grammar, "TParser", "TLexer", "s", "a+b", true);
		assertEquals("(expr a + (expr b))\n", found);

		String expecting =
			"line 1:1 reportAttemptingFullContext d=1 (expr), input='+'\n" +
			"line 1:2 reportContextSensitivity d=1 (expr), input='+b'\n";
		assertEquals(expecting, this.stderrDuringParse);

		found = execParser("T.g4", grammar, "TParser", "TLexer", "s", "a+b*c", true);
		assertEquals("(expr a + (expr b * (expr c)))\n", found);

		expecting =
			"line 1:1 reportAttemptingFullContext d=1 (expr), input='+'\n" +
			"line 1:2 reportContextSensitivity d=1 (expr), input='+b'\n" +
			"line 1:3 reportAttemptingFullContext d=1 (expr), input='*'\n" +
			"line 1:5 reportAmbiguity d=1 (expr): ambigAlts={1, 2}, input='*c'\n";
		assertEquals(expecting, this.stderrDuringParse);
	}

}
