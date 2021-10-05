import java.util.*;

public class AdvancedTopDownParser {
    // 读取到的值
    private enum Type {
        ART, N, V, NP, VP, S, EOS, ERROR
    }

    // 转换规则
    Map<Type, List<Type>> rules;

    AdvancedTopDownParser() {
        this.rules = new HashMap<>();
        this.rules.put(Type.S, Arrays.asList(Type.NP, Type.VP));
        this.rules.put(Type.NP, Arrays.asList(Type.ART, Type.N));
        this.rules.put(Type.VP, Arrays.asList(Type.V, Type.NP));
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

    // 复合元素，比如 NP, VP, S; 可以进一步分解
    class Compound implements Comp {
        Type type;
        List<Comp> e;
        TreeNode node;
        Compound(Type type, List<Comp> e) {
            this.type = type;
            this.e = e;
            ArrayList<TreeNode> nodesList = new ArrayList<>();
            e.forEach(item -> nodesList.add(item.getNode()));
            this.node = new TreeNode(this.type.toString(), nodesList);
        }

        @Override
        public String pretty() {
            StringBuilder s = new StringBuilder("(");
            s.append(this.type.toString());
            s.append(" ");
            for (Comp comp : this.e) {
                s.append(comp.pretty());
                s.append(" ");
            }
            s.append(")");
            return s.toString();
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

    // 通用的 parse 方法
    private Result genericParse(State st, List<String> s, Type startType) {
        if (!this.rules.containsKey(startType)) {
            // 不存在该类型的变换规则, 那么就是原子元素了
            if (st.token == startType) {
                Comp a1 = new Atom(st.value, st.token);
                // 只有处理完原子元素后，才进行 scan
                scan(st, s);
                return new Result(a1, true);
            }
            return new Result(new Atom("error", Type.ERROR), false);
        }

        // 复合元素的处理
        List<Comp> e = new ArrayList<>();
        for (Type type: this.rules.get(startType)) {
            Result res = genericParse(st, s, type);
            if (!res.valid) return new Result(new Atom("error", Type.ERROR), false);
            e.add(res.e);
        }
        return new Result(new Compound(startType, e), true);
    }

    private Comp parseEntry(List<String> s) {
        State st = new State(0, Type.EOS, " ");
        scan(st, s);
        Result res = genericParse(st, s, Type.S);
        if (!res.valid) return new Atom("error", Type.ERROR);
        return res.e;
    }

    private void test(List<String> s) {
        Comp c = parseEntry(s);
        System.out.println(c.pretty());
        c.visualize();
    }

    public static void main(String[] args) {
        String s = "The cat catch a pen".toLowerCase();
        AdvancedTopDownParser atp = new AdvancedTopDownParser();
        atp.test(Arrays.asList(s.split(" ")));
    }

}
