"""
First DSP tests
https://github.com/pierre-rouanet/aupyom
http://zulko.github.io/blog/2014/03/29/soundstretching-and-pitch-shifting-in-python/
https://librosa.github.io/librosa/generated/librosa.effects.pitch_shift.html
https://code.soundsoftware.ac.uk/projects/pyin
"""


import sys
mydir = 'C:/Users/SampsaLaine/git/choir_repository/choir/python/librosa_py3_pYIN-master'
srcpath = mydir + '/src'
sys.path.append(srcpath)

import pYINmain
import numpy as np
from YinUtil import RMS
import librosa
import matplotlib.pyplot as plt
import matplotlib


filename1 = mydir + '/src/oboe-a4.wav'
frameSize = 2048
hopSize = 256

audio, fs = librosa.load(filename1, sr=None)
plt.plot(audio)
plt.show()


pYinInst = pYINmain.PyinMain()
pYinInst.initialise(channels = 1, inputSampleRate = fs, stepSize = hopSize, blockSize = frameSize,
                   lowAmp = 0.25, onsetSensitivity = 0.7, pruneThresh = 0.1)

print("Generating frames...")
pYinInst.m_yin.m_yinBufferSize = int(frameSize/2)
all_frames = librosa.util.frame(audio, frame_length=frameSize, hop_length=hopSize)

print("Processing audio...")
for frame in all_frames.T:
    fs, _ = pYinInst.process(frame)
    
monoPitch = pYinInst.getSmoothedPitchTrack()

# output smoothed pitch track
print("========Results=========")
print("Pitch track")
for ii in fs.m_oSmoothedPitchTrack:
    print(ii.values)
print ('\n')

fs = pYinInst.getRemainingFeatures(monoPitch)

# output of mono notes,
# column 0: frame number,
# column 1: pitch in midi number, this is the decoded pitch
# column 2: attack 1, stable 2, silence 3
print('mono note decoded pitch')
for ii in fs.m_oMonoNoteOut:
    print(ii.frameNumber, ii.pitch, ii.noteState)
print('\n')

print('note pitch tracks')
for ii in fs.m_oNotePitchTracks:
    print(ii)
print('\n')

# median pitch in Hz of the notes
print('median note pitch')
for ii in fs.m_oNotes:
    print(ii.values)
print('\n')


import numpy
import __future__
import librosa

from aupyom import Sound
from aupyom.util import example_audio_file
from aupyom import Sampler

audio_file = example_audio_file()
print(audio_file)

s1 = Sound.from_file(audio_file)

sampler = Sampler()
sampler.play(s1)
sampler.remove(s1)

freq = 440.0
sr = 22050
t = 10


s2 = Sound(numpy.sin(2 * numpy.pi * freq * numpy.linspace(0, t, sr * t)), sr)
sampler.play(s2)
sampler.remove(s2)


