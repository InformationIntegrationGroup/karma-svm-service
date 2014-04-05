#Training part
library(e1071)
args<-commandArgs(TRUE)
# prints the arguments passed
args

# load the data
Train<-read.csv(args[1],head=T,sep=',') #first argument URL
numOfcol<-ncol(Train)
#extract all the columns leaving aside the last column
x<-Train[,-numOfcol]

# get the last column
y<-Train[,numOfcol]

kerneltype<-args[2]
c_type<-args[3]

svmModel<-svm(x,y,kernel=kerneltype,type=c_type)
model=args[4]
modelName<-paste('models/',model,'.RData',sep="")

# save the model to a file
save(svmModel, file=modelName)
print('modelName: ')
modelName
summary(svmModel)