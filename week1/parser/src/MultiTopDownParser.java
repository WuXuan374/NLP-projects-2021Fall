import java.util.*;

/*
* 最终版本：能够生成 AST 的 TopDownParser
* 支持一个成分有多个转换规则
* e.g. NP -> ART N, NP -> ART ADJ N
* */
public class MultiTopDownParser {
    // 读取到的值
    private enum Type {
        ART, N, V, NP, VP, ADJ, S, EOS, ERROR,
    }

    // 转换规则
    Map<Type, List<List<Type>>> rules;

    MultiTopDownParser() {
        this.rules = new HashMap<>();
        this.rules.put(
                Type.S,
                Collections.singletonList(Arrays.asList(Type.NP, Type.VP))
        );
        this.rules.put(
                Type.NP,
                Arrays.asList(
                        Arrays.asList(Type.ART, Type.N),
                        Arrays.asList(Type.ART, Type.ADJ, Type.N)
                )
        );
        this.rules.put(
                Type.VP,
                Arrays.asList(
                        Arrays.asList(Type.V, Type.NP),
                        Collections.singletonList(Type.V)
                )
        );
    }

    interface Comp {
        String pretty();
        void visualize();
        TreeNode getNode();
    }

    // 原子属性: ART, N, V， 不可进一步分解
    static class Atom implements Comp {
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
    static class Compound implements Comp {
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

    static class State {
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
        State copy() {
            return new State(this.startIndex, this.token, this.value);
        }
    }

    static class Result {
        Comp e;
        boolean valid;
        boolean isEOS;
        Result(Comp e, boolean valid, boolean isEOS) {
            this.e = e;
            this.valid = valid;
            this.isEOS = isEOS;
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
                case "beats":
                    st.set(index+1, Type.V, s.get(index));
                    return;
                case "the":
                case "a":
                    st.set(index+1, Type.ART, s.get(index));
                    return;
                case "ugly":
                case "beautiful":
                    st.set(index+1, Type.ADJ, s.get(index));
                    return;
                default: index++;
            }
        }
        st.set(index, Type.EOS, " ");
    }


    private Result genericParse(State st, List<String> s, Type startType) {
        if (!this.rules.containsKey(startType)) {
            // 不存在该类型的变换规则, 那么就是原子元素了
            if (st.token == startType) {
                Comp a1 = new Atom(st.value, st.token);
                // 只有处理完原子元素后，才进行 scan
                scan(st, s);
                if (st.token == Type.EOS) {
                    return new Result(a1, true, true);
                } else {
                    return new Result(a1, true, false);
                }

            }
            return new Result(new Atom("error", Type.ERROR), false, false);
        }

        // 复合元素的处理
        /*
        * 这里有一个 Bug:
        * 能实现预期行为：State _st = st.copy(); st.set(_st.startIndex, _st.token, _st.value);
        * 不能实现预期行为: State _st = st.copy();
        * 体现在例如处理 NP VP 规则，后者对于 NP, VP 的 startIndex 都是 1
        * 原因: Java function pass by value
        * 对于 Object 参数，如果函数内调用 Object 方法，会实际作用在 Object 上
        * 如果函数内调用构造函数，弄了一个新的 Object, 则这个新的 Object 覆盖原 Object, 其修改不作用在原 Object 上
        * 见 https://stackoverflow.com/questions/40480/is-java-pass-by-reference-or-pass-by-value
        * State _st = st.copy(), 在每次递归调用时，实际上新创建了一个 Object
        * 所以前一个 type 可能推进了 st.startIndex，但这个改变是在函数内部对新 Object 的改变，轮到下一个 type 时就没了
        * State _st = st.copy(); st.set(_st.startIndex, _st.token, _st.value);
        * 这种做法只是 setValue, 没有新的 Object, 所以就达到预期
        * */
        State _st = st.copy();
        for (List<Type> types: this.rules.get(startType)) {
            List<Comp> cur = new ArrayList<>();
            boolean isValid = true;
            boolean isEOS = false;
            // 对于每一条规则, 初始状态都要置成一样的
            st.set(_st.startIndex, _st.token, _st.value);
            for (Type type: types) {
                Result res = genericParse(st, s, type);
                if (!res.valid) {
                    isValid = false;
                    break;
                }
                else cur.add(res.e);
                if (res.isEOS) {
                    isEOS = true;
                    break;
                }
            }
            if (isValid) {
                return new Result(new Compound(startType, cur), true, isEOS);
            }
        }
        return new Result(new Atom("error", Type.ERROR), false, false);
    }

    private Comp parseEntry(List<String> s) {
        State st = new State(0, Type.EOS, " ");
        scan(st, s);
        Result res = genericParse(st, s, Type.S);
        if (!res.valid || !res.isEOS) return new Atom("error", Type.ERROR);
        return res.e;
    }

    private void test(List<String> s) {
        Comp c = parseEntry(s);
        System.out.println(c.pretty());
        c.visualize();
    }

    public static void main(String[] args) {
        String s = "The ugly cat receive a beautiful pen".toLowerCase();
        MultiTopDownParser atp = new MultiTopDownParser();
        atp.test(Arrays.asList(s.split(" ")));
    }
}
