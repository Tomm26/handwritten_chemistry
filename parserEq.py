import re
import numpy as np
import scipy as sp

class Tokenize:
    def __init__(self,str):
        self.tokens = list(str.split())

    def getNextToken(self):
        return self.tokens.pop(0)

    def peekNextElement(self, num):
        return self.tokens[num-1] if len(self.tokens) >= num else None
         
class Equation:
    def __init__(self):
        self.elems = [{}, {}]
        self.atoms = []
        self.A = np.zeros((1,1))

    def add(self, side, form, num):
        self.elems[side][form] = num

    def __str__(self):
        return str(self.elems)+'\n'+str(self.atoms)

    def divide(self):
        temp2 = [[], []]

        for i in range(2):

            #for each side of eq 
            for form in self.elems[i]:

                #for each formula
                temp2[i].append([{}])
                param = int(self.elems[i][form])
                li = re.findall("[A-Z][a-z]?\\d*|\\(.*?\\)\\d+", form)

                for elem in li:
                    #for each element in the formula
                    if '(' in elem:
                        mult = int(elem[elem.index(')')+1:])
                        sub = dict(re.findall("([A-Z][a-z]?)(\\d*)",elem[1:elem.index(")")]))
                        sub = {k: int(v)*mult*param if v else mult for k,v in sub.items()}
                        for e in sub:
                            temp2[i][-1][0][e] = sub[e]

                    elif elem[-1].isdigit():
                        sub = re.match(r"([A-Z][a-z]?)(\d*)",elem, re.I).groups()
                        temp2[i][-1][0][sub[0]] = int(sub[1])
                    
                    else:
                        temp2[i][-1][0][elem] = 1

        self.atoms = temp2
        return temp2
    
    def checkCorrectness(self):
        return self.atoms[0].keys() == self.atoms[1].keys()
    
    def balance(self):
        m,n = self.A.shape
        c = np.ones((1,n))
        bA = np.zeros((1,m))
        bound = sp.optimize.Bounds(lb=c[0])

        cnstr = sp.optimize.LinearConstraint(self.A, lb = bA[0], ub = bA[0])
        res = sp.optimize.milp(c[0], integrality=1, bounds = bound, constraints = cnstr)
        return res

    def createMatrix(self):
        #from atoms retrieve the matrix A used for optimization

        allElems = [{}, {}]
        for i in range(2):
            for elem in self.atoms[i]:
                allElems[i] = elem[0].keys() | allElems[i]
                
        A = []

        if allElems[0] == allElems[1]:
                allElems[0] = list(allElems[0])
                for i in range(2):
                    for r in self.atoms[i]:
                        
                        A.append([])
                        for j in range(len(allElems[0])):
                            if r[0].get(allElems[0][j]):
                                A[-1].append(r[0][allElems[0][j]]*(1+i*-2))
                            else:
                                A[-1].append(0)
        self.A = np.array(A).T
        return self.A

#-----------------------------------------------------------------#
#-----------------------------------------------------------------#
    
def isformula(string):
    f = open('useful_txt\\formuleMAX.txt', 'rb')
    for line in f.readlines():
        line = line.decode('utf-8')
        espr = line.split(':')

        if len(espr)>1 and string==espr[1].strip():
            f.close()
            return string
    f.close()
    return string #this should return none, but for debugging
    
def isNumber(string):
    return int(string) if string.isdigit() else None

def isEquation(tk):

    eq = Equation()
    r1 = isSide(tk, eq, 0)
    r2 = tk.getNextToken()
    r3 = isSide(tk, eq, 1)
    
    if r1 and r2=='->' and r3:
        return ('by adding '+r1+'you get '+r3, eq)
    else:
        return ("string is not correctly formed!", None)

def isSide(tk, eq, side):

    res = ""
    if isNumber(tk.peekNextElement(1)) and isformula(tk.peekNextElement(2)):
        
        r1 = isNumber(tk.getNextToken())
        r2 = isformula(tk.getNextToken())
        
        if r1 > 0 and r2:
            res += str(r1) +' '+r2+" "
            eq.add(side,r2, r1)
        else:
            return None
        
        if tk.peekNextElement(1)=='+':
            tk.getNextToken()
            res+= 'and '
            res+=isSide(tk, eq, side)

    elif isformula(tk.peekNextElement(1)):
        r2 = isformula(tk.getNextToken())
        if r2:
            res = '1 '+r2+" "
            eq.add(side, r2,1)
        else:
            return None
    
        if tk.peekNextElement(1)=='+':
            tk.getNextToken()
            res+= 'and '
            res+=isSide(tk, eq, side)

    return res

#-----------------------------------------------------------------#
#-----------------------------------------------------------------#

tk = Tokenize("Fe(NO3)3 + MgO -> Fe2O3 + Mg(NO3)2")

res, eq = isEquation(tk)
res = eq.divide()
eq.createMatrix()
print(eq.balance()["x"])
