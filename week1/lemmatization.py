# NLP-Week1: 形态还原任务
import re


class Lemmatization:
    def __init__(self):
        with open('input/dic_ec.txt', encoding='utf-16-le') as f:
            self.raw_dictionary = f.readlines()
        self.dictionary = self.construct_dictionary(self.raw_dictionary)

    def get_dictionary_item(self, input):
        if input in self.dictionary:
            return ' '.join([input, self.dictionary[input]])
        return None

    def apply_rules(self, input: str):
        if input in self.dictionary:
            return [self.get_dictionary_item(input)]
        if input.endswith('ies'):
            return [self.get_dictionary_item(input[:-3] + 'y')]
        if input.endswith('es'):
            return [self.get_dictionary_item(input[:-2])]
        if input.endswith('s'):
            return [self.get_dictionary_item(input[:-1])]
        if re.match(r".*([a-zA-Z])\1{1}ing$", input):
            return [self.get_dictionary_item(input[:-4])]
        if input.endswith('ying'):
            return [self.get_dictionary_item(input[:-3] + 'ie')]
        if input.endswith('ing'):
            return [self.get_dictionary_item(input[:-3]), self.get_dictionary_item(input[:-3] + 'e')]
        if re.match(r".*.([a-zA-Z])\1{1}ed$", input):
            return [self.get_dictionary_item(input[:-3])]
        if input.endswith('ied'):
            return [self.get_dictionary_item(input[:-3] + 'y')]
        if input.endswith('ed'):
            return [self.get_dictionary_item(input[:-2]), self.get_dictionary_item(input[:-1])]

    def construct_dictionary(self, content):
        dictionary = dict()
        for line in content:
            # [:-1]: 删掉 \n
            line = line.split('\uf8f5')[:-1]
            dictionary[line[0]] = ' '.join(line[1:])
        return dictionary

    def get_result(self, input):
        res = self.apply_rules(input)
        # filter None
        res = list(filter(lambda x: x, res)) if res else None
        # 记录 oov_token
        if not res or len(res) == 0:
            with open('output/oov_token.txt', 'a+') as f:
                f.write(input + '\n')
            return input + " not found"
        return res


if __name__ == '__main__':
    lemma = Lemmatization()
    print(lemma.get_result('plays'))
    print(lemma.get_result('finishes'))
    print(lemma.get_result('flies'))
    print(lemma.get_result('singing'))
    print(lemma.get_result('lying'))
    print(lemma.get_result('stopping'))
    print(lemma.get_result('formed'))
    print(lemma.get_result('denied'))
    print(lemma.get_result('copied'))
    print(lemma.get_result('arbeit'))