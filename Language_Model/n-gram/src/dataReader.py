# NLP-任务2： N-Gram 语言模型的实现，
# 参考 "https://github.com/kaisadadi/n-gram-adding-one-and-good-turing-" 中的实验设置
import re
from os import listdir
from os.path import isfile, join


class DataReader:
    def __init__(self, data_dir, mode="sentence", word=True, type=False):
        """
        :param data_dir: 数据所在目录
        :param mode: "sentence": 句号作为一句话的结束；"line": 一行是一句话
        :param word: True: 单词分割， False: 字分割
        :param type: True: 考虑词性； False: 不考虑词性
        """
        # with open('../stopWords.txt', 'r') as f:
        #     self.stopWords = f.read().split()
        self.stopWords = ['，', '、', '：', '[', ']', '《', '》', '"', '！', '；', '？']
        self.sentences = []
        print(self.stopWords)
        files = [join(data_dir, f) for f in listdir(data_dir) if isfile(join(data_dir, f))]
        for file in files:
            with open(file,  encoding='gbk') as f:
                if mode == "sentence" and word and not type:
                    sents = f.read().split('。/w')
                    for sent in sents:
                        processed_sent = []
                        for word in sent.split():
                            if word.startswith('/') or word.split('/')[0] in self.stopWords:
                                continue
                            processed_sent.append(word.split('/')[0])
                        self.sentences.append(processed_sent)

    def get_sentences(self):
        return self.sentences


# if __name__ == '__main__':
#     dataReader = DataReader("../dataset/train")
#     sents = dataReader.get_sentences()
#     print(len(sents))
#     for idx in range(0, 10):
#         print(sents[idx])