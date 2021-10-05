import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TopDownParser {
    // 读取到的值
    private enum Type {
        ART, N, V, EOS, ERROR
    }

    interface Comp {
        String pretty();
        void visualize();
        TreeNode getNode();
    }

    // 原子属性: ART, N, V， 不可进一步分解
    class Atom implements Comp {
        Type type;
        String value;
        TreeNode node;
        Atom(String value, Type type) {
            this.type = type;
            this.value = value;
            this.node = new TreeNode(this.pretty(), new ArrayList<>());
        }

        @Override
        public String pretty() {
            return "(" + this.type + " " + this.value + ")";
        }

        @Override
        public void visualize() {
            System.out.print(this.node.toString());
        }

        @Override
        public TreeNode getNode() {
            return this.node;
        }
    }

    class NP implements Comp {
        Comp[] a;
        TreeNode node;
        // Version1 NP -> ART N
        NP(Comp a1, Comp a2) {
            a = new Comp[]{a1, a2};
            this.node = new TreeNode("NP", Arrays.asList(a1.getNode(), a2.getNode()));
        }

        @Override
        public String pretty() {
            String s = "(NP ";
            s += a[0].pretty();
            s += " ";
            s += a[1].pretty();
            s += ")";
            return s;
        }

        @Override
        public void visualize() {
            System.out.print(this.node.toString());
        }

        @Override
        public TreeNode getNode() {
            return this.node;
        }
    }

    class VP implements Comp {
        Comp[] a;
        TreeNode node;
        // version1: VP -> V NP
        VP(Comp a1, Comp a2) {
            a = new Comp[]{a1, a2};
            this.node = new TreeNode("VP", Arrays.asList(a1.getNode(), a2.getNode()));
        }

        @Override
        public String pretty() {
            String s = "(VP ";
            s += a[0].pretty();
            s += " ";
            s += a[1].pretty();
            s += ")";
            return s;
        }

        @Override
        public void visualize() {
            System.out.print(this.node.toString());
        }

        @Override
        public TreeNode getNode() {
            return this.node;
        }
    }

    class Sentence implements Comp {
        Comp[] a;
        TreeNode node;
        Sentence(Comp a1, Comp a2) {
            a = new Comp[]{a1, a2};
            this.node = new TreeNode("S", Arrays.asList(a1.getNode(), a2.getNode()));
        }

        @Override
        public String pretty() {
            return a[0].pretty() + " " + a[1].pretty();
        }

        @Override
        public void visualize() {
            System.out.print(this.node.toString());
        }

        @Override
        public TreeNode getNode() {
            return this.node;
        }
    }

    class State {
        int startIndex;
        Type token;
        String value;
        State (int index, Type tok, String value) {
            this.startIndex = index;
            this.token = tok;
            this.value = value;
        }
        void set (int index, Type tok, String value) {
            this.startIndex = index;
            this.token = tok;
            this.value = value;
        }
    }

    class Result {
        Comp e;
        boolean valid;
        Result(Comp e, boolean valid) {
            this.e = e;
            this.valid = valid;
        }
    }

    private void scan(State st, List<String> s) {
        int index = st.startIndex;
        if (index >= s.size()) {
            st.set(index, Type.EOS, " ");
            return;
        }
        while (index < s.size()) {
            switch (s.get(index)) {
                case "cat":
                case "dog":
                case "boy":
                case "pen":
                    st.set(index+1, Type.N, s.get(index));
                    return;
                case "catch":
                case "receive":
                    st.set(index+1, Type.V, s.get(index));
                    return;
                case "the":
                case "a":
                    st.set(index+1, Type.ART, s.get(index));
                    return;
                default: index++;
            }
        }
        st.set(index, Type.EOS, " ");
    }

    private Result parseNP(State st, List<String> s) {
        if (st.token == Type.ART) {
            Comp a1 = new Atom(st.value, st.token);
            scan(st, s);
            if (st.token == Type.N) {
                Comp a2 = new Atom(st.value, st.token);
                Comp c = new NP(a1, a2);
                return new Result(c, true);
            }
        }
        return new Result(new Atom("error", Type.ERROR), false);
    }

    private Result parseVP(State st, List<String> s) {
        if (st.token == Type.V) {
            Comp a1 = new Atom(st.value, st.token);
            scan(st, s);
            Result res = parseNP(st, s);
            if (!res.valid) {
                return new Result(new Atom("error", Type.ERROR), false);
            }
            Comp a2 = res.e;
            Comp c = new VP(a1, a2);
            return new Result(c, true);
        }
        return new Result(new Atom("error", Type.ERROR), false);
    }

    private Comp parse(List<String> s) {
        State st = new State(0, Type.EOS, " ");
        scan(st, s);
        Result res = parseNP(st, s);
        if (!res.valid) return new Atom("error", Type.ERROR);
        Comp a1 = res.e;

        scan(st, s);
        Result res2 = parseVP(st, s);
        if (!res2.valid) return new Atom("error", Type.ERROR);
        Comp a2 = res2.e;

        return new Sentence(a1, a2);
    }

    private void test(List<String> s) {
        Comp c = parse(s);
        System.out.println(c.pretty());
        c.visualize();
    }

    public static void main(String[] args) {
        String s = "The dog receive a pen".toLowerCase();
        TopDownParser tp = new TopDownParser();
        tp.test(Arrays.asList(s.split(" ")));
    }

}
