import re

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
        self.atoms = [{}, {}]
    def add(self, side, form, num):
        self.elems[side][form] = num

    def __str__(self):
        return str(self.elems)+'\n'+str(self.atoms)

    def divide(self):
        temp = [{}, {}]
        for i in range(2):
            for form in self.elems[i]:
                param = int(self.elems[i][form])
                li = re.findall("[A-Z][a-z]?\\d*|\\(.*?\\)\\d+", form)
                for elem in li:
                    if '(' in elem:
                        mult = int(elem[elem.index(')')+1:])
                        sub = dict(re.findall("([A-Z][a-z]?)(\\d*)",elem[1:elem.index(")")]))
                        sub = {k: int(v)*mult*param if v else mult for k,v in sub.items()}
                        temp[i] = {k: temp[i].get(k,0) + sub.get(k,0) for k in set(sub)|set(temp[i])}
                        
                        
                    elif elem[-1].isdigit():
                        sub = re.match(r"([A-Z][a-z]?)(\d*)",elem, re.I).groups()
                        
                        if not temp[i].get(sub[0]):
                            temp[i][sub[0]] = int(sub[1])*param if sub[1] else param
                        else:
                            temp[i][sub[0]]+= int(sub[1])*param
                    elif temp[i].get(elem):
                        temp[i][elem]+=param
                    else:
                        temp[i][elem] = param
        self.atoms = temp
    
    def checkCorrectness(self):
        return self.atoms[0].keys() == self.atoms[1].keys()
    
    def balance(self):
        if self.checkCorrectness():
            for elem in self.atoms[0]:
                break

def isformula(string):
    f = open('ProgettoLocale/formuleScraping/formuleMAX.txt', 'rb')
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

tk = Tokenize("Fe(NO3)3 + 1 MgO -> Fe2O3 + Mg(NO3)2")

res, eq = isEquation(tk)

print(res) if not eq else eq.divide()

print(eq.checkCorrectness())
print(eq)
#print(re.findall("([A-Z][a-z]?)(\\d*)", 'NaCl4P3PO4'))
#print(isformula('H2O'))
