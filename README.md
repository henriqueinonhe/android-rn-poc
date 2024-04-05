# Android + React Native Integration

This is a PoC that aims to showcase how to integrate a pre-existing Android application written in Kotlin with React Native through a new Activity.

The application consists of two activities, the main one that displays a list of notes and another one that's opened when a note is selected and allows that note to be edited.

There are two branches:

- `master`
- `react-native`

In the `master` branch, **both activities** are implemented natively in Kotlin.

In the `react-native` branch, the main activity (notes display) is implemented natively **but the note editing** activity is fully implemented in React Native.

## Requirements

[https://reactnative.dev/docs/environment-setup](https://reactnative.dev/docs/environment-setup)

## Installation

```sh
npm install
```

## Running

First, make sure the emulator is up and running.

```sh
npm run start
```

Then, either press `a` in the terminal load and start the application in the emulator.

Alternatively, open **another** terminal:

```sh
npm run android
``` 

