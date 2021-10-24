# coding:utf-8
import matplotlib.pyplot as plt
from wordcloud import WordCloud
import requests
import json
import codecs

def create_wordcloud(text):

    # 環境に合わせてフォントのパスを指定する。
    #fpath = "/System/Library/Fonts/HelveticaNeue-UltraLight.otf"
    fpath = "/Library/Fonts/ヒラギノ丸ゴ ProN W4.ttc"

    # ストップワードの設定
    stop_words = [u'てる', u'いる', u'なる', u'れる', u'する', u'ある', u'こと', u'これ', u'さん', u'して', \
             u'くれる', u'やる', u'くださる', u'そう', u'せる', u'した',  u'思う',  \
             u'それ', u'ここ', u'ちゃん', u'くん', u'', u'て',u'に',u'を',u'は',u'の', u'が', u'と', u'た', u'し', u'で', \
             u'ない', u'も', u'な', u'い', u'か', u'ので', u'よう', u'']

    wordcloud = WordCloud(background_color="white",font_path=fpath, width=900, height=500, \
                          stopwords=set(stop_words)).generate(text)

    plt.figure(figsize=(15,12))
    plt.imshow(wordcloud)
    plt.axis("off")
    plt.show()

f = codecs.open('web/factor_20170204_delete_urinary_4word_neologd_minipb1_nosetsubi.json', 'r')
word_json = json.load(f)
word_list = word_json['value']
wordtext = ""
for word in word_list:
    print(word['text'])
    wordtext += (word['text'] + " ")*word['weight']

create_wordcloud(wordtext)
