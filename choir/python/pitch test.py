"""
First DSP tests
https://github.com/pierre-rouanet/aupyom
http://zulko.github.io/blog/2014/03/29/soundstretching-and-pitch-shifting-in-python/
https://librosa.github.io/librosa/generated/librosa.effects.pitch_shift.html
"""


from aupyom import Sound
from aupyom.util import example_audio_file

audio_file = example_audio_file()
print(audio_file)

s1 = Sound.from_file(audio_file)

import numpyfrom __future__ import print_function
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
beat_times = librosa.frames_to_time(beat_frames, sr=sr)from __future__ import print_function
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

freq = 440.0
sr = 22050
t = 10


s2 = Sound(numpy.sin(2 * numpy.pi * freq * numpy.linspace(0, t, sr * t)), sr)

from aupyom import Sampler

sampler = Sampler()




sampler.play(s2)

sampler.remove(s1)


def soundwave(freq, t=10, sr=22050):
    return Sound(numpy.sin(2 * numpy.pi * freq * numpy.linspace(0, t, sr * t)), sr)
    

waves = [soundwave(293) for note in range(1, 12)]
# for w in waves:
#     sampler.play(w)
sampler.play(s1)
s1.pitch_shift = 0
s1.pitch_shift = 0.1
s1.pitch_shift = 0.2
 
y, sr = librosa.load(librosa.util.example_audio_file())
   