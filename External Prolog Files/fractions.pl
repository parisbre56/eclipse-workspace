:- module(fractions,[
		fractions2/1,
		fractions3/1,
		fractions4/1,
		fractions5/1,
		fractions6/1,
		fractionsX/3
	]).
	
:- use_module(library(clpfd)).

fractions2((A/BC, D/EF)):-
	SOL=[A,BC,D,EF],
	VARS=[A,B,C,D,E,F],
	fractionsX(2,SOL,VARS),
	label(VARS).

fractions3((A/BC, D/EF, G/HI)):-
				VARS = [A,B,C,D,E,F,G,H,I], %Variables to be used 
				VARS ins 1..9,  %Variable range (not 0)
				all_different(VARS), %each var different than the other
				%some intermediate values
				BC #= B*10+C,EF #= E*10+F,HI #= H*10+I,
				%constraint to eliminate symetrical
				(A*EF*HI) #> (D*BC*HI), (D*BC*HI) #> (G*BC*EF), 
				%And then the fraction result constraint, expressed
				%like this to avoid the problems with floating point division
				(A*EF*HI)+(D*BC*HI)+(G*BC*EF) #= BC*EF*HI,
				%return result as values, not as domains
				labeling([ff,bisect],VARS).
				
fractions4((A/BC, D/EF, G/HI, J/KL)):- 
	SOL=[A,BC,D,EF,G,HI,J,KL],
	VARS=[A,B,C,D,E,F,G,H,I,J,K,L],
	fractionsX(4,SOL,VARS),
	labeling([down],[A,D,G,J]),labeling([up],[BC,EF,HI,KL]).

fractions5((A/BC, D/EF, G/HI, J/KL, M/NO)):- 
	SOL=[A,BC,D,EF,G,HI,J,KL,M,NO],
	VARS=[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O],
	fractionsX(5,SOL,VARS),labeling([down],[A,D,G,J,M]),labeling([up],[BC,EF,HI,KL,NO]).

fractions6((A/BC, D/EF, G/HI, J/KL, M/NO, P/QR)):- 
	SOL=[A,BC,D,EF,G,HI,J,KL,M,NO,P,QR],
	VARS=[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R],
	fractionsX(6,SOL,VARS),labeling([down],[A,D,G,J,M,P]),labeling([up],[BC,EF,HI,KL,NO,QR]).
				
%This solves the fractionsum=1 problem for X number of fractions
fractionsX(NUM,SOL,VARS):-
				NUM > 0, %check that num is an int > 0
				%Returns a list in VARS with NUM unnamed variables
				NNUM is NUM*3, createVarList(VARS,NNUM),
				%ensure they all have the proper value
				VARS ins 1 .. 9,
				%constrain num of occurences
				FLN is floor(NUM/3), CLN is ceiling(NUM/3),
				OCN=[OCN1,OCN2,OCN3,OCN4,OCN5,OCN6,OCN7,OCN8,OCN9],
				OCN ins FLN .. CLN, 
				sum(OCN,#=,NNUM),
				%use global cardinality to ensure number of occurences is within limits
				%(like the all_different we used above only more versatile)
				global_cardinality(VARS,[1-OCN1,2-OCN2,3-OCN3,4-OCN4,5-OCN5,6-OCN6,7-OCN7,8-OCN8,9-OCN9]),
				%This creates the final constraint that the fraction sum must equal 1
				%and that each fraction must be less than the next one (to eliminate symettrical)
				%fraction sum is transformed from A/BC+D/EF=1 into A*EF+D*BC=BC*EF
				%where [A*EF,D*BC]=TRANSFORMED and END=BC*EF
				createTopList(VARS,TOPLIST),createBottomList(VARS,BOTTOMLIST),
				createEnd(BOTTOMLIST,END),transformFracsFirst(BOTTOMLIST,TOPLIST,TRANSFORMED),
				sum(TRANSFORMED,#=,END),
				%returns the solution
				createSolution(VARS,SOL).
				
createTopList([],[]):-!.
createTopList([X|[_|[_|VARS]]],[X|TOPLIST]):-createTopList(VARS,TOPLIST).

createBottomList([],[]):-!.
createBottomList([_|[Y|[Z|VARS]]],[YZ|BOTTOMLIST]):-createBottomList(VARS,BOTTOMLIST),YZ #= Y*10+Z .

createEnd([],1):-!.
createEnd([YZ|BOTTOMLIST],NEND):-createEnd(BOTTOMLIST,END),NEND #= END*YZ.

transformFracsFirst([YZ|BOTTOMLIST],[X|TOPLIST],[RETFRAC|TRANSFORMED]):-
	transformFrac([],X,BOTTOMLIST,RETFRAC),
	transformFracs(RETFRAC,[YZ],BOTTOMLIST,TOPLIST,TRANSFORMED).
	
transformFracs(_,_,[],[],[]):-!.
transformFracs(PREVFRAC,PREVBOTTOM,[YZ|NEXTBOTTOM],[X|TOPLIST],[RETFRAC|TRANSFORMED]):-
	transformFrac(PREVBOTTOM,X,NEXTBOTTOM,RETFRAC),
	PREVFRAC #> RETFRAC ,
	transformFracs(RETFRAC,[YZ|PREVBOTTOM],NEXTBOTTOM,TOPLIST,TRANSFORMED).
	
transformFrac(PREVBOTTOM,X,NEXTBOTTOM,RETFRAC):-
	makeFracPrev(PREVBOTTOM,RETP),makeFracPrev(NEXTBOTTOM,RETN),
	RETFRAC #= X*RETP*RETN .

%creates a list of unnamed variables
createVarList([],0):-!.
createVarList([_|RETL],NUM):- NNUM is NUM - 1, createVarList(RETL,NNUM).
		
makeFracPrev([],1):-!.
makeFracPrev([YZ|F],RET):- makeFracPrev(F,RET1), RET #= YZ*RET1 .

createSolution([],[]):-!.
createSolution([X|[Y|[Z|VARS]]],[X|[YZ|RET]]):- YZ #= Y*10+Z,
	createSolution(VARS,RET).