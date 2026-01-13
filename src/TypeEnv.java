// TypeEnv.java
// Type environment for S

import java.util.*;

// <id, type> 
class Entry {
   Identifier id;
   Type type;
   
   Entry (Identifier id, Type t) {
     this.id = id;
     this.type = t;
   }
}

class TypeEnv extends Stack<Entry> {
    // keys are Identifiers and indexes are memory addresses

    public TypeEnv() { }
    
    public TypeEnv(Identifier id, Type t) {
        push(id, t);
    }
    
    public TypeEnv push(Identifier id, Type t) {
        super.push(new Entry(id, t));
        return this;
    }

    public boolean contains (Identifier v) {
       for (int i=size()-1; i>=0; i--) {
         if (v.equals(((Entry)get(i)).id))
            return true;
       }
       return false;
    }

   // get the type of a variable in a type env. 
   public Type get (Identifier v) {
        Entry p = null;
        for (int i=size()-1; i>=0 && p==null; i--)
           if (v.equals(((Entry)get(i)).id))
              p = (Entry)(get(i));
	if (p != null)
        return (Type)(p.type);
	else 
	    return null;
   }

   public void display( ) {
		System.out.print("{ ");
		String sep = "";
		for (int i=size()-1; i>=0; i--) {
			Entry p=(Entry) get(i);
		    System.out.print(sep + "<" + p.id + ", " + p.type + ">");
		    sep = ", ";
		}
		System.out.println(" }");
   }
}
