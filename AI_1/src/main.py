# -*- coding: utf-8 -*-
'''
Created on 10 Νοε 2014

@author: Parisbre56
'''

import itertools
import sys  # @UnusedImport
from search import *  # @UnusedWildImport
from _sqlite3 import Row

#############Class for the problem solver###########
class SliderBlocksProblem(Problem):
    def __init__(self,input_grid,endState_grid,slideNum,horSizeCon,verSizeCon):
        Problem.__init__(self, tuple(itertools.imap(tuple, input_grid)), tuple(itertools.imap(tuple, endState_grid)))
        self.slideNum=slideNum #The number of sliding blocks (except the red block), possibly 0
        self.horSize=horSizeCon
        self.verSize=verSizeCon
        
    #Checks if there is an empty space for blockNum hMov spaces down (negative means up) 
    #and vMov spaces right (negative means left) 
    def checkDirection(self,blockNum,state,(hMov,vMov)):
        #For all blocks
        for i in range(0,self.verSize):
            for j in range(0,self.horSize):
                #If this is a matching block
                if state[i][j]==blockNum:
                    #If the suggested movement space is not in bounds, return false
                    if (i+vMov)>=self.verSize or (j+hMov)>=self.horSize or (i+vMov)<0 or (j+hMov)<0:
                        return False
                    #If it is occupied by a block other than this one, return false
                    if state[i+vMov][j+hMov]!=0 and state[i+vMov][j+hMov]!=blockNum:
                        return False
                    
        #If no problems were found, return true
        return True
    
    #Moves a block without checking, returns the new state as a copied list
    def moveBlock(self,oldState,(blockNum,(hMov,vMov))):
        state=map(list,oldState)
        #For all blocks
        for i in range(0,self.verSize):
            for j in range(0,self.horSize):
                #If this is a matching block in the old state
                if oldState[i][j]==blockNum:
                    #Mark it as empty in the new state
                    state[i][j]=0
        #For all blocks
        for i in range(0,self.verSize):
            for j in range(0,self.horSize):
                #If this is a matching block in the old state
                if oldState[i][j]==blockNum:
                    #Mark the corresponding position as occupied by this block in the new state
                    state[i+vMov][j+hMov]=blockNum
        #Return the new state
        return tuple(itertools.imap(tuple, state))
        
    #Definition of possible actions in a specific state
    def actions(self,state):
        #Possible actions are all the possible block movements. 
        #For all blocks, tests the four possible directions of movement (1,2,3,4) (up,down,left,right)
        #If that movement is possible, add the tuple to the returned list [(<block_num>,(<block_movement_ver,block_movement_hor>)),...]
        retList=[]
        if self.slideNum>0: #If there are more than 0 blocks
            for blockNum in (1,self.slideNum+1): #For each block
                #Direction up
                if self.checkDirection(blockNum,state,(-1,0))==True:
                    retList.append((blockNum,(-1,0)))
                #Direction down
                if self.checkDirection(blockNum,state,(1,0))==True:
                    retList.append((blockNum,(1,0)))
                #Direction left
                if self.checkDirection(blockNum,state,(0,-1))==True:
                    retList.append((blockNum,(0,-1)))
                #Direction right
                if self.checkDirection(blockNum,state,(0,1))==True:
                    retList.append((blockNum,(0,1)))
        
        #Remember, needs special check for -1 
        #Direction up
        if self.checkDirection(-1,state,(-1,0))==True:
            retList.append((-1,(-1,0)))
        #Direction down
        if self.checkDirection(-1,state,(1,0))==True:
            retList.append((-1,(1,0)))
        #Direction left
        if self.checkDirection(-1,state,(0,-1))==True:
            retList.append((-1,(0,-1)))
        #Direction right
        if self.checkDirection(-1,state,(0,1))==True:
            retList.append((-1,(0,1)))
        
        #Return list of possible choices
        return retList
    
    #Definition of results of selected action
    def result(self,state,action):
        #Action is the movement of the block. It is the number of the block, plus a direction for movement
        return self.moveBlock(state,action)
    
    #Simply checks whether or not the exit position in the goal state matches the current exit position 
    def goal_test(self, state):
        for i in range(0,sizeVer):
            for j in range(0,sizeHor):
                if self.goal[i][j]==-1 and state[i][j]!=-1:
                    return False
        return True 
    
    #Heuristic
    def h(self,n):
        return 1 #TODO: Finish this

#############Set up initial state############
sizeHor=int(raw_input("Give horizontal size:"))
sizeVer=int(raw_input("Give vertical size:"))

#Check for wrong size
if sizeHor<=0 or sizeVer<=0:
    sys.exit(1)

redSquareGiven = False

#Create empty grid
input_grid=[0]*sizeVer
for i in range(0,sizeVer):
    input_grid[i]=[0]*sizeHor

#Get input for grid
breakLoop=False
squareNum=1 #The identifier given to each square
for i in range(0,sizeVer):
    for j in range(0,sizeHor):
        if input_grid[i][j]==0:
            print 'X',range(0,sizeHor)
            for ip in range(0,sizeVer):
                print ip,input_grid[ip]
            print "END"
            inPut=int(raw_input("For grid position "+str(i)+","+str(j)+" give >0 for rectangle, 0 for no rectangle or <0 for stop."))
            if inPut>0: #If this is a rectangle definition
                #Get max vertical size
                for ii in range(i,sizeVer):
                    if input_grid[ii][j]==0:
                        maxRange=ii-i+1
                    else:
                        break
                #Get horizontal size from user if necessary
                if maxRange==1:
                    verSize=1
                else:
                    while(True):
                        verSize=int(raw_input("Give rectangle vertical size (1-"+str(maxRange)+")"))
                        if verSize>0 and verSize<=maxRange:
                            break
                        else:
                            print "Invalid input. Retry."
                
                #Get vertical size
                for jj in range(j,sizeHor):
                    found=False
                    for ii in range(i,verSize):
                        if input_grid[ii][jj]!=0:
                            found=True
                            break
                    if found==True:
                        break
                    else:
                        maxRange=jj-j+1
                #Get vertical size from user if necessary
                if maxRange==1:
                    horSize=1
                else:
                    while(True):
                        horSize=int(raw_input("Give rectangle horizontal size (1-"+str(maxRange)+")"))
                        if horSize>0 and horSize<=maxRange:
                            break
                        else:
                            print "Invalid input. Retry."
                
                #Ask if this is a red square, if legal
                if redSquareGiven==False:
                    isRedSquare=int(raw_input("Is this the red square? Give <=0 for yes and >0 for no"))
                else:
                    isRedSquare=1
                    
                #If this is a red square, give it the special id -1, save the previous ID and save its size
                if isRedSquare<=0: 
                    redSquareGiven=True
                    savedId=squareNum
                    squareNum=-1
                    redSquareHor=horSize
                    redSquareVer=verSize
                
                #Create the square with the given id
                for ii in range(i,i+verSize):
                    for jj in range(j,j+horSize):
                        input_grid[ii][jj]=squareNum
                
                #If this was a red square, restore the previous id
                if isRedSquare<=0:
                    squareNum=savedId
                #Else get the next square id
                else:
                    squareNum=squareNum+1
                    
            elif inPut==0: #If this is an empty square
                pass #Do nothing and continue on with the next square
            else: #For <0, if this is a break loop
                #Check for red square. If it exists, exit the loop
                if redSquareGiven==True:
                    breakLoop=True
                    break
                else:
                    print "You cannot stop giving input. You haven't given a red square yet."
                    j=j-1       
    if breakLoop:
        break
    
#Check for no red square given
if redSquareGiven==False:
    print "You did not give a red square. Cannot continue"
    print 'X',range(0,sizeHor)
    for ip in range(0,sizeVer):
        print ip,input_grid[ip]
    print "END"
    sys.exit(1)

#Get exit orientation
while(True):
    exitSide=int(raw_input("Red square exit position (1 for top, 2 for bottom, 3 for left, 4 for right)"))
    if exitSide<=0 or exitSide>4:
        print "InvalidInput. Retry"
    else:
        break
    
#Get exit position in the axis (position+redSquareSize can't be more than size) 
if exitSide==1 or exitSide==2: #For top and bottom, check horizontal
    maxRange=sizeHor-redSquareHor
else: #For left and right, check vertical
    maxRange=sizeVer-redSquareVer
while(True):
    exitPos=int(raw_input("Give the exit position (0-"+str(maxRange)+")"))
    if exitPos>=0 and exitPos<=maxRange:
        break
    else:
        print "Invalid input, retry"

#Print input for verification
print "Exit side:",
if exitSide==1:
    print "Top"
elif exitSide==2:
    print "Bottom"
elif exitSide==3:
    print "Left"
elif exitSide==4:
    print "Right"
    
print "Exit position in axis: ",exitPos

print ""

print 'X',range(0,sizeHor)
for ip in range(0,sizeVer):
    print ip,input_grid[ip]
print "END"

print ""

#Create empty grid
endState_grid=[-2]*sizeVer #-2 means we don't care
for i in range(0,sizeVer):
    endState_grid[i]=[-2]*sizeHor

#Get end position
if exitSide==1:
    for i in range(0,redSquareVer):
        for j in range(exitPos,exitPos+redSquareHor):
            endState_grid[i][j]=-1    
elif exitSide==2:
    for i in range(sizeVer-1,sizeVer-1-redSquareVer):
        for j in range(exitPos,exitPos+redSquareHor):
            endState_grid[i][j]=-1
elif exitSide==3:
    for i in range(exitPos,exitPos+redSquareVer):
        for j in range(0,redSquareHor):
            endState_grid[i][j]=-1
elif exitSide==4:
    for i in range(exitPos,exitPos+redSquareVer):
        for j in range(sizeHor-1,sizeHor-1-redSquareHor):
            endState_grid[i][j]=-1

print "END_STATE"
print 'X',range(0,sizeHor)
for ip in range(0,sizeVer):
    print ip,endState_grid[ip]
print "END"

sliderProblem=SliderBlocksProblem(input_grid,endState_grid,squareNum-1,sizeHor,sizeVer)
aNode=astar_search(sliderProblem)

print "Solved the probem in "+str(len(aNode.path())-1)+" moves."
print ""
print "Solutions follow:"
for solutionStep in aNode.path():
    for row in solutionStep:
        print row
    print"End of solution step"
    print ""
print"End of solution"

sys.exit(0)