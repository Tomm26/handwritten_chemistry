import os
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0' #for tensorflow messages suppression
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '1'
import cv2
import math
import numpy as np
import tensorflow as tf
from keras.models import load_model

#segment.py takes an image and returns the guessed string

dic = ['(', ')', '0', '1', '2', '3', '4','5','6','7','8','9','A','b','C','d','e','f','G','H','i','j','k','l','M','N','o','p','plus','q','R','rightarrow','S','T','u','v','w','X','y','z']
dic = sorted(dic)
size = 45 #pixel shape of each symbol

model = load_model("model_DILATED.hdf5")

def readElems():
    #reads all the elements symbols
    elems = []
    f = open('useful_txt\\periodicTable.txt', 'r')
    for el in f.readlines():
        elems.append(el.strip())
    return elems

def make_square(w,h, img):

    dim = max(w,h)
    res = np.zeros((dim,dim),np.uint8)
    res.fill(255)

    nH = math.floor(h/2)
    nW = math.floor(w/2)
    nD = math.floor(dim/2)

    res[nD-nH:nD+(h-nH), nD-nW:nD+(w-nW)] = img[0:h, 0:w]
    return res

def get_divs(img, thd):

    #get_divs finds each symbol by grouping the black pixels distant thd between each other
    divs = []
    newd = True
    whites = 0

    # Convert the image to a boolean array where True represents non-zero values
    img_bool = img > 0.003

    # Find the sum of non-zero elements along each column
    col_sums = np.sum(img_bool, axis=0)

    # Iterate over the column sums
    for j, col_sum in enumerate(col_sums):
        if newd and col_sum > 0:
            divs.append([j, -1])
            newd = False
        elif not newd and col_sum > 0:
            divs[-1] = [divs[-1][0], j]

        if col_sum == 0:
            whites += 1
            if whites >= thd:
                newd = True
                whites = 0

    return divs

def get_predictions(img, maxs):

    #get_predictions returns a list of maxs predicted symbols
    img = tf.expand_dims(img, 0)
    pred = model.predict(img, verbose=0)
    i=0
    probs = {}
    for c in dic:
        probs[c] = pred[0][i]
        i+=1

    probs = dict(sorted(probs.items(), key=lambda item: item[1], reverse=True))
    i = 0
    res = []

    for c in probs:
        res.append(c)
        i+=1
        if i>=maxs:
            break
    return res

def err_correction(temp, cl, cbl: True, cbn: True, df):
    #temp is the current string guessed, cl the new symbol the fun returns the "correct" symbol
    elems = readElems()
    could_be_letter = cbl
    could_be_number = cbn

    if len(cl) == 0:
        return df

    if cl[0] == '0':
        return 'O'#mmmmmm
    if len(cl[0]) == 1 and cl[0].isalpha():
        #symbol is letter, if it's in the table (like O) return, 
        #else check if with the previous is in table
        if cl[0] in elems or (cl[0].upper() in [e[0] for e in elems]):
            return cl[0]
        elif len(temp) > 0 and temp[-1].upper()+cl[0].lower() in elems:
            return cl[0]
        else:
            could_be_letter = False

    if (len(cl[0])) == 1 and cl[0].isdigit():
        #symbol is number, if it's at the beginning or is not after a ) or after an elem then it could not be num
        if len(temp) == 0 or (temp[-1] not in elems and temp[-1] == ')' and (len(temp)>1 and temp[-2:] not in elems)):
            could_be_number = False
        else:
            return cl[0]

    if could_be_letter and could_be_number:
        #it's neither a letter nor a number
        return cl[0] #for now...
    return err_correction(temp, cl[1:], could_be_letter, could_be_number, df)

#---------------------------------------------------------------------------------#
#---------------------------------------------------------------------------------#

img = cv2.imread('images_for_testing\\real4.jpg')
gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)

#binarize and segment 
ret,thresh = cv2.threshold(gray,127,255,cv2.THRESH_BINARY_INV)
divs = get_divs(thresh, 5) #find columns divisors

seg_imgs = []
eps = 5
res = ""

for i in divs:

    #for each colums divisor, find the row divisor and reshape the image to fit the model shape (45x45)
    seg_imgs.append(thresh[:,i[0]:i[1]+eps])
    newd = get_divs(np.transpose(seg_imgs[-1]), 5)

    seg_imgs[-1] = seg_imgs[-1][newd[0][0]:newd[0][1]+eps, :]
    _, seg_imgs[-1] = cv2.threshold(seg_imgs[-1],127,255,cv2.THRESH_BINARY_INV)

    image = make_square(seg_imgs[-1].shape[1],seg_imgs[-1].shape[0], seg_imgs[-1])
    image = cv2.resize(image, (size,size), interpolation = cv2.INTER_AREA)

    #get the prediction and try to correct it 
    pr = get_predictions(image, 3)
    res+=err_correction(res,pr , True, True, pr[0])+" "

print("la formula è ", res)