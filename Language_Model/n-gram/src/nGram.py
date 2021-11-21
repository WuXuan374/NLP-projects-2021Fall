import json

from dataReader import DataReader

import collections
import os
import math


class nGram:
    def __init__(self, n, count_path="", freq_path=""):
        """
        n: n元语言模型
        count_path: 记录 n-gram 出现的次数, dictionary
        freq_path: 记录 n-gram 出现的相对频率，dictionary
        """
        self.n = n
        self.freq_path = freq_path
        self.test_sents = DataReader('/Users/wuxuan/Documents/nju-21-22-1/研一上/自然语言处理/NLP-projects-2021Fall/Language_Model/n-gram/dataset/test').get_sentences()
        if self.freq_path != "":
            with open(self.freq_path, 'r', encoding='utf-8') as f:
                self.ngram_freq = json.load(f)

        else:
            self.count_path = count_path
            if self.count_path != "":
                with open(self.count_path, 'r', encoding='utf-8') as f:
                    self.ngram_count = json.load(f)
            else:
                self.train_sents = DataReader('/Users/wuxuan/Documents/nju-21-22-1/研一上/自然语言处理/NLP-projects-2021Fall/Language_Model/n-gram/dataset/train').get_sentences()
                self.ngram_count = self.compute_ngram_count_with_add_one(n)

    def compute_ngram_count_with_add_one(self, n):
        """
        基于 add-one 平滑策略，计算每个 n-gram 出现次数
        add-one: 所有 n-gram 出现的次数都 + 1
        哪些 n-gram 作为 key: 训练集和测试集出现的所有
        """
        count_dict = dict()
        # start_token: <s>
        if n == 1:
            for sent in self.train_sents:
                for word in sent:
                    if word not in count_dict:
                        count_dict[word] = 2
                    count_dict[word] += 1

            for sent in self.test_sents:
                for word in sent:
                    if word not in count_dict:
                        count_dict[word] = 1
        elif n == 2:
            for sent in self.train_sents:
                for idx in range(0, len(sent)):
                    if idx == 0:
                        if '<s>' not in count_dict:
                            count_dict['<s>'] = collections.defaultdict(int)
                        if count_dict['<s>'][sent[idx]] == 0:
                            count_dict['<s>'][sent[idx]] += 2
                        else:
                            count_dict['<s>'][sent[idx]] += 1
                    else:
                        if sent[idx-1] not in count_dict:
                            count_dict[sent[idx-1]] = collections.defaultdict(int)
                        if count_dict[sent[idx-1]][sent[idx]] == 0:
                            count_dict[sent[idx - 1]][sent[idx]] += 2
                        else:
                            count_dict[sent[idx - 1]][sent[idx]] += 1

            for sent in self.test_sents:
                for idx in range(0, len(sent)):
                    if idx == 0:
                        if '<s>' not in count_dict:
                            count_dict['<s>'] = collections.defaultdict(int)
                        if count_dict['<s>'][sent[idx]] == 0:
                            count_dict['<s>'][sent[idx]] = 1
                    else:
                        if sent[idx - 1] not in count_dict:
                            count_dict[sent[idx - 1]] = collections.defaultdict(int)
                        if count_dict[sent[idx-1]][sent[idx]] == 0:
                            count_dict[sent[idx - 1]][sent[idx]] = 1
        return count_dict

    def get_ngram_count(self):
        return self.ngram_count

    def get_ngram_freq(self):
        return self.ngram_freq

    def compute_ngram_freq(self):
        """
        已经得到 ngram_count 之后，计算 ngram_freq
        :return: ngram_freq
        """
        if not self.ngram_count:
            return None
        freq = dict()
        if self.n == 1:
            total = sum(self.ngram_count.values())
            for key in self.ngram_count:
                freq[key] = self.ngram_count[key]/total
        elif self.n == 2:
            for key in self.ngram_count:
                freq[key] = dict()
                total = sum(self.ngram_count[key].values())
                for item in self.ngram_count[key]:
                    freq[key][item] = self.ngram_count[key][item]/total
        return freq

    def compute_sentence_probability(self, sentence):
        """
        计算一个句子，在该语言模型下的出现概率
        对于二元模型， P(W) = P(w_1) \prod_{i=2...n} P(w_i|w_{i-1})
        :return:
        """
        prob = 1
        for idx in range(0, len(sentence)):
            if self.n == 1:
                prob *= self.ngram_freq[sentence[idx]]
            elif self.n == 2:
                dic = self.ngram_freq[sentence[idx-1] if idx - 1 >= 0 else '<s>']
                prob *= dic[sentence[idx]]

        return prob

    def compute_perplexity(self):
        """
        计算该语言模型的困惑度：通过计算测试集的存在的概率来实现
        公式见 PPT 27
        """
        # 所有句子的概率乘积
        prob = 0
        sents_len = len(self.test_sents)
        for sent in self.test_sents:
            cur = self.compute_sentence_probability(sent)
            if cur == 0:
                continue
            prob += math.log(cur)
        # 没有进行数据平滑的话，会发现 prob = 0, 计算不出困惑度
        l = 1/sents_len * prob
        perplexity = math.pow(2, -l)
        return perplexity


if __name__ == '__main__':
    # unigram = nGram(1)
    # unigram_dict = unigram.get_ngram_count()
    # with open('output/unigram.json', 'w', encoding="utf-8") as f:
    #     json.dump(unigram_dict, f, ensure_ascii=False, indent=4)

    # bigram = nGram(2)
    # bigram_dict = bigram.get_ngram_count()
    # with open('output/bigram.json', 'w', encoding="utf-8") as f:
    #     json.dump(bigram_dict, f, ensure_ascii=False, indent=4)

    # bigram = nGram(2, count_path="output/bigram.json")
    # freq = bigram.compute_ngram_freq()
    # with open('output/bigram_freq.json', 'w', encoding="utf-8") as f:
    #     json.dump(freq, f, ensure_ascii=False, indent=4)
    # print(unigram.get_ngram_count())

    # unigram = nGram(1, count_path="output/unigram.json")
    # freq = unigram.compute_ngram_freq()
    # with open('output/unigram_freq.json', 'w', encoding="utf-8") as f:
    #     json.dump(freq, f, ensure_ascii=False, indent=4)

    # bigram = nGram(2, freq_path="output/bigram_freq.json")
    # print(bigram.get_ngram_freq())

    bigram = nGram(2, freq_path="output/bigram_freq.json")
    print(bigram.compute_perplexity())
    sent = ["国务院", "发出",  "紧急", "通知", "确保", "减负"]
    print(bigram.compute_sentence_probability(sent))

    unigram = nGram(1, freq_path="output/unigram_freq.json")
    print(unigram.compute_perplexity())

    # bigram_freq = bigram.get_ngram_freq()
    # print(sum(bigram_freq.values()))
