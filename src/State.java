import java.util.*;
// State as stack 

// <id, val> 
class Pair {
   Identifier id;
   Value val;
   
   Pair (Identifier id, Value v) {
     this.id = id;
     this.val = v;
   }
}

class State extends Stack<Pair> {
    public State( ) { }
    
    public State(Identifier id, Value val) {
        push(id, val);
    }
    
    public State push(Identifier id, Value val) {
        super.push(new Pair(id, val));
	return this;
    }

	
    public int lookup (Identifier v) {
       for (int i=size()-1; i>=0; i--) 
         if (v.equals(((Pair)get(i)).id))
            return i;

       return -1;
   }

    // get the value of a variable in a State
    public Value get (Identifier id) {
	int i = lookup(id);
        if (i == -1) 
            return null;
        Pair p = (Pair)(get(i));
        return (Value)(p.val);
    }

    public State set(Identifier id, Value val) {
	int i = lookup(id);
        if (i != -1) 
            super.set(i, new Pair(id, val));  // ArrayList.set
	return this;
    }

    public void display( ) {
        System.out.print("{ ");
        String sep = "";
        for (int i=size()-1; i>=0; i--) {
            Pair p = (Pair) get(i);
            System.out.print(sep + "<" + p.id + ", " + p.val + ">");
            sep = ", ";
        }
        System.out.println(" }");
   }
}
