# -*- coding: utf-8 -*-
"""
Created on Sat Apr 25 08:34:44 2020

@author: SampsaLaine
"""


from __future__ import print_function
import librosa

# 1. Get the file path to the included audio example
filename = librosa.util.example_audio_file()

# 2. Load the audio as a waveform `y`
#    Store the sampling rate as `sr`
y, sr = librosa.load(filename)

# 3. Run the default beat tracker
tempo, beat_frames = librosa.beat.beat_track(y=y, sr=sr)

print('Estimated tempo: {:.2f} beats per minute'.format(tempo))

# 4. Convert the frame indices of beat events into timestamps
beat_times = librosa.frames_to_time(beat_frames, sr=sr)

import soundfile as sf

# Get example audio file
filename = librosa.util.example_audio_file()
data, samplerate = sf.read(filename, dtype='float32')