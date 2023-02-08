// IAlexaMediaServiceCallback.aidl
package com.quest.macaw.media.common;

// Declare any non-default types here with import statements

interface IAlexaMediaServiceCallback {

   void onPlayerStateChanged(boolean playWhenReady, int playbackState);

   void onProgressToAlexa(long position, long duration);

   void sendControlAction(int action);

   void unregisterCallback(boolean isFromAlexa);

   void processMediaError(String message);

   void onPlayerError(String errorMsg);

   void onSourceChanged(String sourceType, boolean isFromAlexa);
 }