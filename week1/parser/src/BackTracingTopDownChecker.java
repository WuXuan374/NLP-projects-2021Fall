import java.util.*;

/*
* 通过回溯方式，支持一个成分有多个转换规则
* e.g. NP -> ART N, NP -> ART ADJ N
* 但是，这部分代码还只能实现句法检查，没法生成句法树
*  */
public class BackTracingTopDownChecker {
    // 读取到的值
    private enum Type {
        ART, N, V, ADJ, NP, VP, S, EOS, ERROR
    }

    class State {
        ArrayList<Type> types;
        int index;
        State(ArrayList<Type> types, int index) {
            this.types = types;
            this.index = index;
        }
        public void match() {
            this.index++;
        }
        public void extend(int index, List<Type> newTypes) {
            this.types.remove(index);
            this.types.addAll(index, newTypes);
        }
        public State copy() {
            // 注意点: ArrayList 必须复制一份，否则传的是引用
            // 亦即两个 State 会指向同一个 ArrayList types, 导致异常行为
            ArrayList<Type> newTypes = new ArrayList<>(this.types);
            return new State(newTypes, this.index);
        }
    }

    // 转换规则
    Map<Type, List<List<Type>>> rules;
    Stack<State> st;
    List<Type> wordsList;

    BackTracingTopDownChecker(List<String> s) {
        this.rules = new HashMap<>();
        this.rules.put(
                Type.S,
                Arrays.asList(Arrays.asList(Type.NP, Type.VP))
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
                        Arrays.asList(Type.V)
                )
        );

        this.st = new Stack<>();
        this.wordsList = scan(s);
    }

    private List<Type> scan(List<String> s) {
        List<Type> types = new ArrayList<>();
        for (String item: s) {
            switch (item) {
                case "cat":
                case "dog":
                case "boy":
                case "pen":
                    types.add(Type.N);
                    break;
                case "catch":
                case "receive":
                    types.add(Type.V);
                    break;
                case "the":
                case "a":
                    types.add(Type.ART);
                    break;
                case "ugly":
                case "beautiful":
                    types.add(Type.ADJ);
                    break;
            }
        }
        return types;
    }

    public boolean backTracing() {
        // 回溯: 取栈里头找一个 State
        if (!st.empty()) {
            State _s = st.peek();
            st.pop();
            return semanticChecker(_s);
        } else {
            return false;
        }
    }


    public boolean semanticChecker(State s) {
        // 结束条件: 遍历到 State 中的最后一个 Type
        if (s.index == s.types.size()) {
            if (s.index == wordsList.size()) return true;
            return backTracing();
        }

        // 每次递归只要比较一个 Type
        Type type = s.types.get(s.index);
        if (!rules.containsKey(type)) {
            // 原子元素，直接匹配
            if (type == this.wordsList.get(s.index)) {
                s.match();
                return semanticChecker(s);
            } else {
                return backTracing();
            }
        } else {
            // 非原子元素，把所有规则加入栈中
            for (List<Type> children: this.rules.get(type)) {
                State s_copy = s.copy();
                s_copy.extend(s.index, new ArrayList<>(children));
                st.push(s_copy);
            }
            return backTracing();
        }
    }

    public boolean checkEntry() {
        State s = new State(new ArrayList<>(Collections.singletonList(Type.S)), 0);
        return semanticChecker(s);
    }

    private void test() {
        System.out.println(checkEntry());
    }

    public static void main(String[] args) {
        String s = "A cat catch the pen".toLowerCase();
        BackTracingTopDownChecker btp = new BackTracingTopDownChecker(Arrays.asList(s.split(" ")));
        btp.test();
    }

}
