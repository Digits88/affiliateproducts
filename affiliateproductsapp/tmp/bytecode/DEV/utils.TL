37ca1546af9b79ba68a586c6fcad7f8 ����   3 b  utils/TL  java/lang/Object threadSession Ljava/lang/ThreadLocal; 	Signature 0Ljava/lang/ThreadLocal<Ldto/ThreadLocalParams;>; <clinit> ()V Code  java/lang/ThreadLocal
    
 <init>	     LineNumberTable LocalVariableTable
   this 
Lutils/TL; set %(Ljava/lang/Long;Ljava/lang/String;)V
     get ()Ldto/ThreadLocalParams;
  !   dto/ThreadLocalParams " # 	setUserId (Ljava/lang/Long;)V
  % & ' setRequestId (Ljava/lang/String;)V userId Ljava/lang/Long; 	requestId Ljava/lang/String; setToken
  . , ' token 	setSource
  2 0 ' source 
setChannel
  6 4 ' channel setUser (Lmodels/shopyourway/SywUser;)V
  ; 8 9 user Lmodels/shopyourway/SywUser;
  ?  @ ()Ljava/lang/Object;
  
  C  D (Ljava/lang/Object;)V localParams Ldto/ThreadLocalParams; StackMapTable getToken ()Ljava/lang/String;
  K H I 	getUserId ()Ljava/lang/Long;
  O L M getRequestId
  R P I 	getSource
  U S I 
getChannel
  X V I getUser ()Lmodels/shopyourway/SywUser;
  \ Y Z remove
  _ ] 
 
SourceFile TL.java !      
           	 
     /      � Y� � �       
    
 
          
     /     *� �                        	       K     � *� � +� $�                        ( )      * +  	 , '     6     � *� -�       
               / +   	 & '     6     � *� $�       
               * +   	 " #     6     � *� �       
               ( )   	 0 '     6     � *� 1�       
               3 +   	 4 '     6     � *� 5�       
    !  "         7 +   	 8 9     6     � *� :�       
    %  &         < =   	       g     � � >� K*� � Y� AK� *� B*�           ) 
 *  +  ,  .      
  E F   G    �   	 H I     '      � � J�           2       	 L M     '      � � N�           5       	 P I     '      � � Q�           9       	 S I     '      � � T�           =       	 V I     '      � � W�           A       	 Y Z     '      � � [�           E       	 ] 
     +      � � ^�       
    I  J        `    a