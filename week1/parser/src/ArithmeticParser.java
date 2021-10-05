/* Reimplement ArithmeticParser */

import java.util.ArrayList;
import java.util.Arrays;

public class ArithmeticParser {
    // constants
    private enum Type {
        TypeIll, TypeInt
    }

    // 读取到的值
    private enum Value {
        EOS, ZERO, ONE, TWO, OPEN, CLOSE, MULT, PLUS
    }

    // AST elements
    interface Exp {
        String pretty();
        Type infer();
        void visualize();
        TreeNode getNode();
    }

    class Num implements Exp {
        int value;
        TreeNode node;
        Num (int value) {
            this.value = value;
            this.node = new TreeNode(this.pretty(), new ArrayList<>());
        }
        public String pretty() {
            return Integer.toString(value);
        }
        public Type infer() {
            return Type.TypeInt;
        }
        public void visualize() {
            System.out.print(this.node.toString());
        }
        public TreeNode getNode() { return this.node; }
    }

    class Mult implements Exp{
        Exp[] e;
        TreeNode node;
        Mult (Exp e1, Exp e2) {
            e = new Exp[]{e1, e2};
            this.node = new TreeNode("*", Arrays.asList(e1.getNode(), e2.getNode()));
        }
        public String pretty() {
            String s = "";
            s += e[0].pretty();
            s += "*";
            s += e[1].pretty();
            return s;
        }
        public Type infer() {
            if (e[0].infer() == Type.TypeInt && e[1].infer() == Type.TypeInt) return Type.TypeInt;
            return Type.TypeIll;
        }
        public void visualize() {
            System.out.print(this.node.toString());
        }
        public TreeNode getNode() { return this.node; }
    };

    class Plus implements Exp{
        Exp[] e;
        TreeNode node;
        Plus (Exp e1, Exp e2) {
            e = new Exp[]{e1, e2};
            this.node = new TreeNode("+", Arrays.asList(e1.getNode(), e2.getNode()));
        }
        public String pretty() {
            String s = "";
            s += e[0].pretty();
            s += "+";
            s += e[1].pretty();
            return s;
        }
        public Type infer() {
            if (e[0].infer() == Type.TypeInt && e[1].infer() == Type.TypeInt) return Type.TypeInt;
            return Type.TypeIll;
        }
        public void visualize() {
            System.out.print(this.node.toString());
        }
        public TreeNode getNode() { return this.node; }
    }

    class Parenthesis implements Exp {
        Exp e;
        TreeNode node;
        Parenthesis(Exp e) {
            this.e = e;
            this.node = new TreeNode("()", Arrays.asList(e.getNode()));
        }

        public String pretty() {
            String s = "";
            s += "(";
            s += e.pretty();
            s += ")";
            return s;
        }

        public Type infer() {
            return e.infer();
        }

        public void visualize() {
            System.out.print(this.node.toString());
        }

        public TreeNode getNode() {
            return this.node;
        }
    }

    class State {
        int startIndex;
        Value token;
        State (int index, Value tok) {
            this.startIndex = index;
            this.token = tok;
        }
        void set (int index, Value tok) {
            this.startIndex = index;
            this.token = tok;
        }
    }

    class Result {
        Exp e;
        boolean valid;
        Result (Exp e, boolean valid) {
            this.e = e;
            this.valid = valid;
        }
        void set (Exp e, boolean valid) {
            this.e = e;
            this.valid = valid;
        }
    }

    private void scan(State st, String s) {
        int index = st.startIndex;
        if (index >= s.length()) {
            st.set(index, Value.EOS);
            return;
        }
        while (index < s.length()) {
            switch (s.charAt(index)) {
                case '0':
                    st.set(index+1, Value.ZERO);
                    return;
                case '1':
                    st.set(index+1, Value.ONE);
                    return;
                case '2':
                    st.set(index+1, Value.TWO);
                    return;
                case '(':
                    st.set(index+1, Value.OPEN);
                    return;
                case ')':
                    st.set(index+1, Value.CLOSE);
                    return;
                case '*':
                    st.set(index+1, Value.MULT);
                    return;
                case '+':
                    st.set(index+1, Value.PLUS);
                    return;
                default: // simply skip this token
                    index++;
            }
        }
    }

    private Result parseE(State st, String s) {
        Result res = parseT(st, s);
        if (!res.valid) {
            return new Result(res.e, false);
        }
        return parseE2(st, res.e, s);
    }

    private Result parseE2(State st, Exp e, String s) {
        if (st.token == Value.PLUS) {
            scan(st, s);
            Result res = parseT(st, s);
            if (!res.valid) return new Result(e, false);
            Exp t = new Plus(e, res.e);
            return parseE2(st, t, s);
        }
        return new Result(e, true);
    }

    private Result parseT(State st, String s) {
        Result res = parseF(st, s);
        if (!res.valid) {
            return new Result(res.e, false);
        }
        return parseT2(st, res.e, s);
    }

    private Result parseT2(State st, Exp e, String s) {
        if (st.token == Value.MULT) {
            scan(st, s);
            Result res = parseF(st, s);
            if (!res.valid) {
                return new Result(e, false);
            }
            Exp t = new Mult(e, res.e);
            return parseT2(st, t, s);
        }
        return new Result(e, true);
    }

    private Result parseF(State st, String s) {
        switch (st.token) {
            case ZERO:
                scan(st, s);
                return new Result(new Num(0), true);
            case ONE:
                scan(st, s);
                return new Result(new Num(1), true);
            case TWO:
                scan(st, s);
                return new Result(new Num(2), true);
            case OPEN:
                scan(st, s);
                Result res = parseE(st, s);
                if (!res.valid) {
                    return new Result(res.e, false);
                }
                if (st.token != Value.CLOSE) {
                    return new Result(res.e, false);
                }
                scan(st, s);
                return new Result(new Parenthesis(res.e), true);
        }
        return new Result(new Num(-1), false);
    }

    private Exp parse(String s) {
        State st = new State(0, Value.EOS);
        scan(st, s);
        Result res = parseE(st, s);
        if (st.token == Value.EOS && res.valid) return res.e;
        return new Num(-1);
    }

    private void test(String s) {
        Exp e = parse(s);
        System.out.println(e.pretty());
        System.out.println(e.infer());
        e.visualize();
    }

    public static void main(String[] args) {
        String s = "1+(2*1+1)+1";
        ArithmeticParser ap = new ArithmeticParser();
        ap.test(s);
    }

}
