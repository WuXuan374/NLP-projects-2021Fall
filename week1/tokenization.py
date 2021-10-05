# Week1: 分词
# 消岐没给出具体规则，做不了

# 分词
class Tokenization:
    def __init__(self):
        with open('input/dic_ce.txt', encoding='utf-8') as f:
            self.raw_dictionary = f.readlines()
        self.dictionary = self.construct_dictionary(self.raw_dictionary)

    def construct_dictionary(self, raw):
        dictionary = dict()
        for line in raw:
            [word, explanation] = line.split(',', 1)
            if word:
                dictionary[word] = explanation
        return dictionary

    def forward_maximum_matching(self, sentence, start=0):
        """
        正向最大匹配(FMM), 从左到右，每次取存在于词典的最长的词，构成分词序列
        :param sentence: 原句
        :param start: 本次分词的起始位置
        :return: list of words
        """
        # 递归的终止条件
        if sentence[start: len(sentence)] in self.dictionary:
            return [sentence[start: len(sentence)]]
        for i in range(len(sentence)-1, start, -1):
            if sentence[start: i] in self.dictionary:
                return [sentence[start: i]] + self.forward_maximum_matching(sentence, i)
        return ['']

    def reverse_maximum_matching(self, sentence, end):
        """
        逆向最大匹配，RMM， 从右到左，每次取存在于词典的最长的词，构成分词序列
        :param sentence: 原句
        :param end: 本次分词的最右位置
        :return: list of words
        """
        # 递归终止条件
        if sentence[0: end+1] in self.dictionary:
            return [sentence[0: end+1]]
        for i in range(1, end+1):
            if sentence[i: end+1] in self.dictionary:
                return [sentence[i: end+1]] + self.reverse_maximum_matching(sentence, i-1)
        return ['']

    def reverse_minumum_matching(self, sentence, end):
        """
        逆向最小匹配，从右到左，每次取存在于词典的最短的词，构成分词序列
        :param sentence: 原句
        :param end: 本次分词的最右位置
        :return: list of words
        """
        for i in range(end, 0, -1):
            if sentence[i: end+1] in self.dictionary:
                return [sentence[i: end+1]] + self.reverse_maximum_matching(sentence, i-1)
        if sentence[0: end+1] in self.dictionary:
            return sentence[0: end+1]
        return ['']

    def bidirectional_maximum_matching(self, sentence):
        """
        调用 FMM 和 RMM 的结果，如果一致，则确定分词结果；不一致，则返回两者结果，下一步进行消岐
        :param sentence:
        :return: list, len = 1 || len = 2
        """
        fmm_res = self.forward_maximum_matching(sentence, 0)
        rmm_res = self.reverse_maximum_matching(sentence, len(sentence))
        rmm_res.reverse()
        if fmm_res == rmm_res:
            return [fmm_res]
        return [fmm_res, rmm_res]

    def forward_maximum_reverse_minimum_matching(self, sentence):
        """
        正向最大，逆向最小匹配， 如果一致，则确定分词结果；不一致，则返回两者结果，下一步进行消岐
        :param sentence:
        :return: list, len = 1 || len = 2
        """
        fmm_res = self.forward_maximum_matching(sentence, 0)
        rmimimum_res = self.reverse_minumum_matching(sentence, len(sentence))
        rmimimum_res.reverse()
        if fmm_res == rmimimum_res:
            return [fmm_res]
        return [fmm_res, rmimimum_res]


if __name__ == '__main__':
    tokenization = Tokenization()
    sentence = '讨论战争与和平的相关议题'
    # RMM 的结果，需要 reverse
    print(tokenization.forward_maximum_reverse_minimum_matching(sentence))
