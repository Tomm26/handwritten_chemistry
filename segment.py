import cv2
import math
import numpy as np
import tensorflow as tf
from keras.models import load_model

dic = ['(', ')', '0', '1', '2', '3', '4','5','6','7','8','9','A','b','C','d','e','f','G','H','i','j','k','l','M','N','o','p','plus','q','R','rightarrow','S','T','u','v','w','X','y','z']
dic = sorted(dic)
model = load_model("model_v12.hdf5")

def make_square(w,h, img):

    dim = max(w,h)
    res = np.zeros((dim,dim),np.uint8)
    res.fill(255)

    nH = math.floor(h/2)
    nW = math.floor(w/2)
    nD = math.floor(dim/2)

    res[nD-nH:nD+(h-nH), nD-nW:nD+(w-nW)] = img[0:h, 0:w]
    return res

def get_predictions(img, maxs):
    img = tf.expand_dims(img, 0)
    pred = model.predict(img)
    i=0
    probs = {}
    for c in dic:
        probs[c] = pred[0][i]
        i+=1

    probs = dict(sorted(probs.items(), key=lambda item: item[1], reverse=True))
    i = 0
    for c in probs:
        print(c+": "+str(probs[c]))
        i+=1
        if i>maxs:
            break
    
img = cv2.imread('test3.jpeg')
gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)

#binarize 
ret,thresh = cv2.threshold(gray,127,255,cv2.THRESH_BINARY_INV)

cv2.imshow("thresh",thresh)
cv2.waitKey(0)

#find contours
ctrs, hier = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

#sort contours
sorted_ctrs = sorted(ctrs, key=lambda ctr: cv2.boundingRect(ctr)[0])

for _,ctr in enumerate(sorted_ctrs):

    x, y, w, h = cv2.boundingRect(ctr)

    #cv2.rectangle(img,(x,y),(x+w,y+h),(0,255,0),2)
    _, thresh = cv2.threshold(gray,127,255,cv2.THRESH_BINARY)
    roi = thresh[y:y+h, x:x+w]
    image = make_square(w,h,roi)
    image = cv2.resize(image, (45,45), interpolation = cv2.INTER_AREA)

    cv2.imshow("test",image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    get_predictions(image, 5)