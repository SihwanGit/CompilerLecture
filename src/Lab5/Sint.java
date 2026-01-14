package Lab5; 
// Sint.java
// Interpreter for S
import java.util.Scanner;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) { 
	    if (c instanceof Decl) {
	        Decls decls = new Decls();
	        decls.add((Decl) c);
	        return allocate(decls, state);
	    }

	    if (c instanceof Function) {  //state에다 push
	        Function f = (Function) c; 
	        state.push(f.id, new Value(f)); 
	        return state;
	    }

	    if (c instanceof Stmt)
	        return Eval((Stmt) c, state); 
		
	    throw new IllegalArgumentException("no command");
    }

    State Eval(Stmt s, State state) {
        if (s instanceof Empty) 
	        return Eval((Empty)s, state);
        if (s instanceof Assignment)  
	        return Eval((Assignment)s, state);
        if (s instanceof If)  
	        return Eval((If)s, state);
        if (s instanceof While)  
	        return Eval((While)s, state);
        if (s instanceof Stmts)  
	        return Eval((Stmts)s, state);
	    if (s instanceof Let)  
	        return Eval((Let)s, state);
	    if (s instanceof Read)  
	        return Eval((Read)s, state);
	    if (s instanceof Print)  
	        return Eval((Print)s, state);
        if (s instanceof Call) 
	        return Eval((Call)s, state); //함수 호출 부분 추가
	    if (s instanceof Return) 
	        return Eval((Return)s, state);
        throw new IllegalArgumentException("no statement");
    }

    // [Function]
    // call without return value
    State Eval(Call c, State state) { //return 값이 없는 상태 전이 함수
    	// TODO: [Fill the code of call stmt]
	    Value v = state.get(c.fid);
        Function f = v.funValue();
        state = newFrame(state, c, f); 
        state = Eval(f.stmt, state);
        state = deleteFrame(state, c, f);
        
	    return state;
    }
    
    // [Function]
    // value-returning call 
    Value V (Call c, State state) { 
	    Value v = state.get(c.fid);  	 	// find function
        Function f = v.funValue();
        state = newFrame(state, c, f); 		// create the frame on the stack
        state = Eval(f.stmt, state); 	   	// interpret the call
	    v = state.peek().val;				// get the return value
        state = deleteFrame(state, c, f); 	// delete the frame from the stack
        return v; //얘는 V가 리턴
    }
    
    // [Function]
    State Eval(Return r, State state) {
        Value v = V(r.expr, state); //state를 리턴하므로 peek()가 필요없다.
        return state.set(new Identifier("return"), v); 
    }

    // [Function]
    State newFrame (State state, Call c, Function f) { // 스택 프레임 생성
        if (c.args.size() == 0) 
            return state;
        
        // TODO: [Fill the code of newFrame]
    	// evaluate arguments
        Value val[] = new Value[c.args.size()]; //arguments가 계산된 값들이 val에 저장
        int i=0;
        for (Expr e : c.args) 
            val[i++] = V(e,state); //각각 계산후 val에 계속 저장
        
	    // activate a new stack frame in the stack 
        i=0; 
        for (Decl d : f.params) { // pass by value
            Identifier v = (Identifier)d.id;
            state.push(v, val[i++]); //매개변수 passing 파트
        }
        
        state.push(new Identifier("return"), null); // allocate for return value
        return state; //return을 맨 마지막에 push해서 스택의 맨 위에 올라오게끔 만든 것.
    }

    // [Function]
    State deleteFrame (State state, Call c, Function f) {
    // free a stack frame from the stack
        state.pop();  // pop the return value	 //pop으로 삭제
        
        // TODO: [Fill the code of deleteFrame]
        if (f.params != null) 
            state  = free(f.params, state); //para 개수 만큼 pop 후
       
        return state;            
    }

    State Eval(Empty s, State state) {
        return state;
    }
  
    State Eval(Assignment a, State state) {
        Value v = V(a.expr, state);

        if (a.ar == null)
	        return state.set(a.id, v);
        else {
            Value i = V(a.ar.expr, state);
            Value val = state.get(a.ar.id);
            Value[] ar = (Value[]) val.arrValue();
            ar[i.intValue()] = v;
            return state;
        }
    }

    State Eval(Read r, State state) {
        if (r.id.type == Type.INT) {
	        int i = sc.nextInt();
	        state.set(r.id, new Value(i));
	    } 
 
	    if (r.id.type == Type.BOOL) {
	        boolean b = sc.nextBoolean();	
            state.set(r.id, new Value(b));
	    }

	    if (r.id.type == Type.STRING) {
	        String s = (String) sc.next();
	        state.set(r.id, new Value(s));
	    } 
	    return state;
    }

    State Eval(Print p, State state) {
	    System.out.println(V(p.expr, state));
        return state; 
    }
  
    State Eval(Stmts ss, State state) {
        for (Stmt s : ss.stmts) {
            state = Eval(s, state);
            if (s instanceof Return)  
                return state;
        }
        return state;
    }
  
    State Eval(If c, State state) {
        if (V(c.expr, state).boolValue( ))
            return Eval(c.stmt1, state);
        else
            return Eval(c.stmt2, state);
    }
 
    State Eval(While l, State state) {
        if (V(l.expr, state).boolValue( ))
            return Eval(l, Eval(l.stmt, state));
        else 
	        return state;
    }

    State Eval(Let l, State state) {
        State s = allocate(l.decls, state);
        
        // [Function]
        if (l.funs != null) { //let 문에 fun을 정의하는 부분.
            // TODO: [Fill the code here]
            // Case with Function declaration inside Let
        	for (Function fun : l.funs) //funs가 있다면
        		s.push(fun.id, new Value(fun)); //각각의 fun의 Value를 push
        }
        
        s = Eval(l.stmts, s);

	    return free(l.decls, s);
    }

    State allocate (Decls ds, State state) {
        if (ds != null)
        for (Decl decl : ds) 
		    if (decl.arraysize > 0) { 
                Value[] ar = new Value[decl.arraysize];
                state.push(decl.id, new Value(ar)); 
            }
            else if (decl.expr == null)
                state.push(decl.id, new Value(decl.type));
	        else
		        state.push(decl.id, V(decl.expr, state));

        return state;
    }

    State free (Decls ds, State state) {
        if (ds != null)
        for (Decl decl : ds) 
            state.pop();

        return state;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) {
        check(!v1.undef && !v2.undef,"reference to undef value");
	    switch (op.val) {
	    case "+":
            return new Value(v1.intValue() + v2.intValue());
        case "-": 
            return new Value(v1.intValue() - v2.intValue());
        case "*": 
            return new Value(v1.intValue() * v2.intValue());
        case "/": 
            return new Value(v1.intValue() / v2.intValue());
        case "==":
        	if (v1.type == Type.STRING)
        		return new Value(v1.value == v2.value);
        	else if (v1.type == Type.BOOL)
        		return new Value(v1.boolValue() == v2.boolValue());
        	else
        		return new Value(v1.intValue() == v2.intValue());
        case "!=":
        	if (v1.type == Type.STRING)
        		return new Value(v1.value != v2.value);
        	else if (v1.type == Type.BOOL)
        		return new Value(v1.boolValue() != v2.boolValue());
        	else
        		return new Value(v1.intValue() != v2.intValue());
        case "<":
        	if (v1.type == Type.STRING) {
            	String v1_str = (String)v1.value;
            	String v2_str = (String)v2.value;
            	int res = v1_str.compareTo(v2_str);
            	boolean res_value;
            	if (res < 0) res_value = true;
            	else res_value = false;
            	return new Value(res_value);
        	}
        	else
        		return new Value(v1.intValue() < v2.intValue());
        case "<=":
        	if (v1.type == Type.STRING) {
            	String v1_str = (String)v1.value;
            	String v2_str = (String)v2.value;
            	int res = v1_str.compareTo(v2_str);
            	boolean res_value;
            	if (res <= 0) res_value = true;
            	else res_value = false;
            	return new Value(res_value);
        	}
        	else
        		return new Value(v1.intValue() <= v2.intValue());
        case ">":
        	if (v1.type == Type.STRING) {
            	String v1_str = (String)v1.value;
            	String v2_str = (String)v2.value;
            	int res = v1_str.compareTo(v2_str);
            	boolean res_value;
            	if (res > 0) res_value = true;
            	else res_value = false;
            	return new Value(res_value);
        	}
        	else
        		return new Value(v1.intValue() > v2.intValue());
        case ">=":
        	if (v1.type == Type.STRING) {
            	String v1_str = (String)v1.value;
            	String v2_str = (String)v2.value;
            	int res = v1_str.compareTo(v2_str);
            	boolean res_value;
            	if (res >= 0) res_value = true;
            	else res_value = false;
            	return new Value(res_value);
        	}
        	else
        		return new Value(v1.intValue() >= v2.intValue());
        case "&": 
            return new Value(v1.boolValue() && v2.boolValue());
        case "|": 
            return new Value(v1.boolValue() || v2.boolValue());            

	    default:
	        throw new IllegalArgumentException("no operation");
	    }
    } 
    
    Value unaryOperation(Operator op, Value v) {
        check( !v.undef, "reference to undef value");
	    switch (op.val) {
        case "!": 
            return new Value(!v.boolValue( ));
	    case "-": 
            return new Value(-v.intValue( ));
        default:
            throw new IllegalArgumentException("no operation: " + op.val); 
        }
    } 

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Expr e, State state) {
        if (e instanceof Value) 
            return (Value) e;
        if (e instanceof Identifier) {
	        Identifier v = (Identifier) e;
            return (Value)(state.get(v));
	    }
        if (e instanceof Array) {
	        Array ar = (Array) e;
            Value i = V(ar.expr, state);
            Value v = (Value) state.get(ar.id);
            Value[] vs = v.arrValue(); 
            return (vs[i.intValue()]); 
	    }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Value v1 = V(b.expr1, state);
            Value v2 = V(b.expr2, state);
            return binaryOperation (b.op, v1, v2); 
        }
        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Value v = V(u.expr, state);
            return unaryOperation(u.op, v); 
        }
        if (e instanceof Call) 
    	    return V((Call)e, state);  
        throw new IllegalArgumentException("no operation");
    }

    public static void main(String args[]) {
	    if (args.length == 0) {
	        Sint sint = new Sint(); 
			Lexer.interactive = true;
            System.out.println("Language S Interpreter 1.0");
            System.out.print(">> ");
	        Parser parser  = new Parser(new Lexer());

	        do { // Program = Command*
	            if (parser.token == Token.EOF)
		            parser.token = parser.lexer.getToken();
	       
	            Command command=null;
                try {
	                command = parser.command();
	                 //    if (command != null) command.display(0);    // display AST    
					 if (command == null) 
						 throw new Exception();
					 else  { 
						 command.type = TypeChecker.Check(command); 
//						  System.out.println("\nType: "+ command.type); 
				     }
                } catch (Exception e) {
                    System.out.println("Error: " + e);
		            System.out.print(">> ");
                    continue;
                }

	            if (command.type != Type.ERROR) {
                    System.out.println("\nInterpreting..." );
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                         System.err.println("Error: " + e);  
                    }
                }
		        System.out.print(">> ");
	        } while (true);
	    }
        else {
	        System.out.println("Begin parsing... " + args[0]);
	        Command command = null;
	        Parser parser  = new Parser(new Lexer(args[0]));
	        Sint sint = new Sint();

	        do {	// Program = Command*
	            if (parser.token == Token.EOF)
                    break;
	         
                try {
		            command = parser.command();
					//  if (command != null) command.display(0);      // display AST
					if (command == null) 
						 throw new Exception();
					else  {
						 command.type = TypeChecker.Check(command); 
//                         System.out.println("\nType: "+ command.type);  
				    }
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                    continue;
                }

	            if (command.type!=Type.ERROR) {
                    System.out.println("\nInterpreting..." + args[0]);
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println("Error: " + e);  
                    }
                }
	        } while (command != null);
        }        
    }
}