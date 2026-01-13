// TypeChecker.java
// Static type checker for S

public class TypeChecker {

    static TypeEnv tenv = new TypeEnv();

    public static Type Check(Command p) {
        if (p instanceof Decl) {
            Decl d = (Decl) p;
	    if (tenv.contains(d.id)) 
                error(d, "duplicate variable declaration"); 
	    else
                return Check(d,tenv);
        }
	
     	if (p instanceof Stmt)
	         return Check((Stmt) p, tenv); 
		
	     throw new IllegalArgumentException("undefined command");
    } 

    public static Type Check(Decl decl, TypeEnv te) {
        if (decl.arraysize == 0 && decl.expr != null) 
	    if (decl.type != Check(decl.expr, te))
	        error(decl, "type error: incorrect initialization to " + decl.id);

        te.push (decl.id, decl.type);
        return decl.type;
    }
    
    static Type duplicate(Decls ds) {
        for (int i=0; i<ds.size() - 1; i++)
            for (int j=i+1; j<ds.size(); j++) {
                Decl di = ds.get(i);
                Decl dj = ds.get(j);
                check (!di.id.equals(dj.id), "duplicate declaration: " + dj.id);
            }
	    return Type.VOID;
    } 

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    static void error(Command c, String msg) {
        c.type = Type.ERROR;
        System.err.println(msg);
    }

    static Type checkError(boolean test, Type t, String error) {
        if (test) return  t;
        else {
            System.err.println(error);
            return Type.ERROR;
        }
    }

    static Type Check(Expr e, TypeEnv te) {
        if (e instanceof Value) {
	        Value v = (Value)e;
            return v.type;
	    }

        if (e instanceof Identifier) { 
            Identifier id = (Identifier) e;
            if (!te.contains(id)) 
                error(id, "undeclared variable: " + id);
	        else id.type = te.get(id); 
                return id.type;
        }

        if (e instanceof Array) { 
            Array ar = (Array) e;
            if (!te.contains(ar.id)) 
                error(ar, "undeclared variable: " + ar.id);
	        else if (Check(ar.expr, te) == Type.INT)
		        ar.type = te.get(ar.id); 
	        else
		        error(ar, "non-int index: " + ar.expr);
            return ar.type;
        }

        if (e instanceof Binary) 
            return Check((Binary) e, te); 

        if (e instanceof Unary) 
            return Check((Unary) e, te); 
        throw new IllegalArgumentException("undefined operator");
    }

    static Type Check(Unary u, TypeEnv te) {
        System.err.println("Unary: " + u.op);
        Type t1 = Check(u.expr, te);

        switch (u.op.val) {
        case "!":
            if (t1 == Type.BOOL)
                u.type = Type.BOOL;
            else error(u, "! has non-bool operand");
            break;
        case "-":
            if (t1 == Type.INT)
                u.type = Type.INT;
            else error(u,  "Unary - has non-int operand");
            break;
        default:
            throw new IllegalArgumentException("undefined operator");
        }

        return u.type;
    }

    static Type Check(Binary b, TypeEnv te) {
        Type t1 = Check(b.expr1, te);
        Type t2 = Check(b.expr2, te);

        switch(b.op.val) {
        case "+": case "-": case "*": case "/":
            if (t1 == Type.INT && t2 == Type.INT)
	            b.type = Type.INT;
	        else
	             error(b, "type error for " + b.op);
            break;
        case "<": case "<=": case ">": case ">=": case "==": case "!=":
             if (t1 == t2)
		         b.type = Type.BOOL;
	         else 	
		         error(b, "type error for " + b.op);
             break;
        case "&": case "|": 
            if ( t1 == Type.BOOL && t2 == Type.BOOL)
		        b.type = Type.BOOL;
	        else
		        error(b, "type error for " + b.op);
             break;
        default: 
             throw new IllegalArgumentException("undefined operator");
        }

        return b.type;
    }

    public static Type Check(Stmt s, TypeEnv te) {
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        if (s instanceof Empty) 
	        return Type.VOID;
        if (s instanceof Assignment) 
            return Check((Assignment) s, te);
        if (s instanceof Stmts) 
           return Check((Stmts) s, te);
	    if (s instanceof Read) 
            return Check((Read) s, te);		
        if (s instanceof Print) 
            return Check((Print) s, te);
        if (s instanceof If) 
           return Check((If) s, te);
        if (s instanceof While) 
           return Check((While) s, te);
        if (s instanceof Let) 
           return Check((Let) s, te);
        throw new IllegalArgumentException("undefined statement");
    }

    static Type Check(Print p, TypeEnv te) {
        Type t = Check(p.expr,te);
        if (t != Type.ERROR)
	        p.type = Type.VOID;
        else
            error(p, "type error in expr: " + p.expr);
        return p.type;
    }

    static Type Check(Read r, TypeEnv te) {
	    Type t = Check(r.id, te);
        if ( t == Type.INT || t == Type.BOOL || t==Type.STRING)
	        r.type = Type.VOID;
	    else
	        error(r, " undefined variable in read: " + r.id);
        return r.type;
     }

    static Type Check(Assignment a, TypeEnv te) {
        if (a.ar != null) { // array
            if (!te.contains(a.ar.id)) {
                error(a, " undefined variable in assignment: " + a.ar.id);
                return Type.ERROR;
            }
            Type t1 = te.get(a.ar.id);
            Type t2 = Check(a.ar.expr, te);
            Type t3 = Check(a.expr, te);
	        if (t1 == t3 && t2 == Type.INT) 
	            a.type = Type.VOID;
	        else
	            error(a, "mixed mode assignment to " + a.ar.id + " : " + t1 + " <- " + t2);  
            return a.type;
	    }

	    if (!te.contains(a.id)) {
            error(a, " undefined variable in assignment: " + a.id);
            return Type.ERROR;
        }
        Type t1 = te.get(a.id);
        Type t2 = Check(a.expr, te);
	    if (t1 == t2) 
	        a.type = Type.VOID;
	    else
	        error(a, "mixed mode assignment to " + a.id + " : " + t1 + " <- " + t2);  
        return a.type;
    }

    static Type Check(If c, TypeEnv te) {
       Type t  = Check(c.expr,te);
       Type t1 = Check(c.stmt1, te);
       Type t2 = Check(c.stmt2, te);
       if (t == Type.BOOL)
           if (t1 == t2)
	           c.type = t1;
           else 
	           error(c, "non-equal type in two branches");
       else
    	   error(c, "non-bool test in condition");
       return c.type;
    }

    static Type Check(While l, TypeEnv te) {
        Type t = Check(l.expr,te);
        Type t1 = Check(l.stmt, te);
        if (t == Type.BOOL)
	        if (t1 == Type.VOID)
	            l.type = t1;
	        else   
                error(l, "return in loop..");
	    else
	         error(l, "non-bool test in loop"); 
	    // System.out.println(l.type);
        return l.type;
    }

    static Type Check(Stmts ss, TypeEnv te) {
	    Type t = Type.VOID;
        for (int i=0; i < ss.stmts.size(); i++) {
            t = Check(ss.stmts.get(i), te);
            if (t != Type.VOID && i != ss.stmts.size()-1)
                error(ss, "return in block");
        }

        if (ss.type != Type.ERROR) 
			ss.type = t;
        return ss.type;
    }

    static Type Check(Let l, TypeEnv te) {
        // System.out.println("\nType env:");
        addType(l.decls, te); 
        // te.display();
	    l.type = Check(l.stmts, te);
        deleteType(l.decls, te); 
	    return l.type;
    }

    public static TypeEnv addType (Decls ds, TypeEnv te) {
        // put the variable decls into a symbol table(TypeEnv) 
        if (ds != null) 
        for (Decl decl : ds) 
	    Check(decl, te); 

        return te;
    }

    static TypeEnv deleteType(Decls ds, TypeEnv te) {
        if (ds != null)
        for (Decl decl : ds)
            te.pop();
        // te.display();
        return te;
    }

    public static void main(String args[]) {
        Command command = null;
        if (args.length == 0) {
            Sint sint = new Sint(); 
			Lexer.interactive = true;
            System.out.print(">> ");
            Parser parser  = new Parser(new Lexer());

            do { // Program = Command*
                if (parser.token==Token.EOF) 
                    parser.token = parser.lexer.getToken();
                try { 
                    command = parser.command();
                    // if (command != null) command.display(0);    // display AST
					if (command == null) 
						 throw new Exception();
					 else {  
						 command.type = TypeChecker.Check(command); 
					     System.out.println("\nCommand type :" + command.type);
				     }
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                    System.out.print(">> ");
                    continue;
                }
                System.out.print(">> ");
            } while(true);
        }
        else {
    	    System.out.println("Begin parsing... " + args[0]);
            Parser parser  = new Parser(new Lexer(args[0]));
            do {
                if (parser.token==Token.EOF) 
                    break;
                try {
                    command = parser.command();
                    // if (command != null) command.display(0);           // display AST
					if (command == null) 
						 throw new Exception();
					 else {  
						 command.type = TypeChecker.Check(command); 
					     System.out.println("\nCommand type :" + command.type);
				     }
                } catch (Exception e) {
                    System.err.println("Error: " + e);
                    continue;
                }
            } while (command != null);
        }
    } //main
}