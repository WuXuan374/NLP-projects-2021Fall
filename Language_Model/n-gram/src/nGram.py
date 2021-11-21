from dataReader import DataReader

import collections
import os


class nGram:
    def __init__(self, n):
        """ n: n元语言模型"""
        self.train_sents = DataReader('/Users/wuxuan/Documents/nju-21-22-1/研一上/自然语言处理/NLP-projects-2021Fall/Language_Model/n-gram/dataset/train').get_sentences()
        self.ngram_dict = self.compute(n)

    def compute(self, n):
        freq_dict = collections.defaultdict(int)
        # start_token: <s>
        for sent in self.train_sents:
            for idx in range(0, len(sent)):
                lst = []
                for i in range(idx-n+1, idx+1):
                    if i >= 0:
                        lst += [sent[i]]
                    else:
                        lst += '<s>'
                freq_dict[tuple(lst)] += 1
        return freq_dict

    def get_ngram_dict(self):
        return self.ngram_dict


if __name__ == '__main__':
    unigram = nGram(1)
    print(unigram.get_ngram_dict())