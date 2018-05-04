'''
Hangman.py
'''

import sys
import random
from sqlalchemy.sql.expression import false
from sympy.logic.inference import valid

class Hangman:
    '''
    Initializes the words list
    '''
    def __init__(self):
        file = open('words.txt','r')
        self.words = []
        self.wordguess = []
        for line in file:
            self.words.append(line.rstrip())

    '''
    Outputs the current status of the guesses
    '''
    def printword(self):
        for c in self.wordguess:
            print c,
        print
        #credit to Alice Vich                

        
    def playgame(self):
        valid = False
        maxGuesses = 0
        wordLength = 0
        while not valid:
            valid = True
            maxGuesses = int(raw_input('Enter the number of guesses'))
            if maxGuesses < 1:
                valid = False
                    
        valid = False
        while not valid:
            valid = True
            wordLength = int(raw_input('Enter the Word Length'))
            if wordLength < 3:
                valid = False
            
            for i in self.words:
                if len(i) == wordLength:
                    break
                else:
                    valid = False
                
        guesses = 0
        possibleWords = []
        for j in self.words:
            if len(j) == wordLength:
                possibleWords.append(j)
                
        guessedLetters = []
        
        while guesses < maxGuesses:
            ch = raw_input('Enter a guess:').lower()
            if (not ch.isalpha):
                print "Only alphabetic characters are allowed."
            else:
                if (ch in guessedLetters):
                    print "The Letter", ch, "has already been guessed."
                else:
                    guesses+=1
                    print "You have", maxGuesses - guesses, "guesses remaining."
                    guessedLetters.append(ch)
                    print ' '.join(possibleWords)
                    possibleWords = possibleWords.largestFamilyWords(ch)
                    
                    '''
                    if (ch not in word):
                        print ch, "does not occur."
                    else: 
                        compareLetter = lambda x,y: (x == ch and ch) or y
                        self.wordguess = map(compareLetter, word, self.wordguess)
                        print ' '.join(self.wordguess)
                        if ('_' not in self.wordguess):
                            print "Congratulations!"
                            quit()
                    '''           
        print "Sorry dude the word is", possibleWords.pop()
        
    def findLargestFamily(self, wordList, ch):               
        wordFamilies = dict()
        endStatus = ''
        maxCount = 0
        for words in wordList:
            status = ''
            for letter in words:
                if(letter == ch):
                    status += ch
                else:
                    status += '_'
            if (status not in wordFamilies):
                wordFamilies[status] = 1
            else:
                wordFamilies[status] = wordFamilies[status] + 1
                 
        for wordFamily in wordFamilies:
            if wordFamilies[wordFamily] > maxCount:
                maxCount = wordFamilies[wordFamily]    
                endStatus = wordFamily
        return endStatus
        
        
    def largestFamilyWords(self, wordList, ch):
        endStatus = wordList.findLargestFamily(ch)
        familyToReturn = []
        for words in wordList:
            status = ''
            for letter in words:
                if (letter == ch):
                    status += ch
                else:
                    status += '_'
            if (status == endStatus):
                familyToReturn.append(words)
        return familyToReturn
            
if __name__ == "__main__":

    game = Hangman()

    game.playgame()
