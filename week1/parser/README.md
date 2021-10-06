## Parser
### 最终目标是实现课件上的自顶向下句法分析算法
- 但是这个算法比较难，要回溯
- 而且只能判断句法对不对，没法保存句法结构，生成句法树
### 先复习一下大三上的项目，ArithMeticParser
- 实现的是支持 0 1 2 + - ( ) 运算的 parser
- 基本结构(源代码是 go, Java 复现)
  - 类型定义: enum, 包括表达式类型 Type, 元素值 Value
  - Exp interface:
    - pretty()
    - infer(): 表达式类型
    - visualize(): 语法树可视化相关
    - getNode(): 语法树可视化相关
  - 其他表达式类型，实现 Exp interface，都是 class
    - Num: value
    - Mult: Exp[2]
    - Plus: Exp[2]
    - Parenthesis: Exp e
  - State, Result 两个结构的定义
  - scan(): 读取字符串里的下一个字符，得到解析结果(Value 类型)
  - parse():
    - 改写了规则，避免左递归和 ambiguity
    - 对于每一个成分，有且只有一个确定的规则
    - ![](pic/parse_rules.png)
  - 额外增加的：StackOverflow 上看到的TreeNode 类，配合实现AST的可视化

### 接下来实现 TopDownParser
- 只实现了如下规则
  - S -> NP VP
  - NP -> ART N
  - VP -> V NP
- 基本是在 ArithMeticParser 基础上做简单修改得到的
- 局限性: 一个成分，只支持一条转换规则，无法在多条规则中进行选择

### AdvancedTopDownParser
- 回顾上面两个 Parser, 有如下缺陷
  - 通过重写规则，避免模糊性，重写规则工作量很大
  - 每个复合类型对应一个 parse 方法
    - NP -> parseNP
    - VP -> parseVP
- 这些缺陷是可以改进的
  - 我们的语法规则，左右是没有相同元素的，不需要避免模糊性，不用重写规则
  - 不同复合类型的 parse, 其原理一致，完全可以抽成一个公共函数
- 实际改进
  - 定义了两种成分， Atom 和 Compound
    - Atom: 原子类型，不能进一步分解，type 指示具体是 N, ART 还是 V
    - Compound： 复合类型，存在子节点
  - 用 Map 存放规则
  - genericParse(..., startType)
    - startType 是原子元素：读取token， 返回Atom
    - 是复合元素：按照转换规则，分成多个 Type 进行 parse, 组合 parse 的结果（数组），构成复合类型
- 总结：不需要给每个复合类型写一个 parse 方法了，方便多了

### BackTracingTopDownParser
- 想在上一个 parser 基础上直接支持多个语法规则的选择、回溯。但是太难了，先实现课件上的算法
- 更准确地说，这只是一个 checker, 只能确定句法是否正确，没法解析语法，得到语法树
- 保存的State 包括当前访问的下标 (index), 目前已处理的 Type types
  - 提供 match() 和 extend() 方法
  - 直接用一个 wordsList 结构，保存所有单词的词性
- SemanticChecker()
  - 典型的回溯递归函数
  - 结束条件: 访问到 State 中最后一个 Type
    - 如果也是 wordsList() 中最后一个元素 True
    - else false
  - 每次递归只处理一个 Type
    - 原子 Type
      - 直接匹配，继续递归
      - 不匹配，回溯，去栈里找一个 State
    - 非原子 Type
      - 把所有规则加入栈中 （copy(), extend())
      - 出栈一个State, 继续递归

### MultiTopDownParser
- 最终版本
  - 能够生成AST
  - 支持一个成分有多条规则
- Result(Comp e, boolean isValid, boolean isEOS)
```java
  for (rule: rules) {
  for (type: types)
  }
```
- genericParse()
  - 原子元素
    - 匹配
      - Result(Comp c, isValid=true, isEOS=(st.token == EOS))
    - 不匹配: (error, false, false)
  - 非原子元素

  - 遍历时，记录 isValid, isEOS, 最后 return
  - 注意点：针对每条规则，初始状态要置成一样的
    - st.set(_st.startIndex, _st.token, _st.value);
- parseEntry()
  - 最后同时检查 isValid, isEOS

### 遇到的 Java 语言特性问题
- copy 一个object时，如果成员变量有object, 比如 ArrayList
  - 不能直接传这个 Object 到构造函数里，必须也复制一份
  - ArrayList<Type> newTypes = new ArrayList<>(this.types);
  - Java pass-by-value, 如果是对象，则是把对象的引用作为 value 传进去(?)
  - 如果直接传 ArrayList, 则两个对象修改的是同一个 ArrayList
- MultiTopDownParser 里头：复制 State, 对于每一条规则, 初始状态都要置成一样的
```java
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
```