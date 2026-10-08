// "Butit Kulit", kuwento at iginuhit ni Joan D. Mayo. Pag-aari ng DepEd Manila, hindi ipinagbibili.
// The only story in the mobile app. Each page has its drawing (assets/images/butit) and the 3D models
// that appear on that page in AR. In AR the whole printed page is the Vuforia Image Target: the same
// page pictures are in unity/BasaKitaAR/Assets/StreamingAssets/pages/<arTarget>.jpg.
import type { Story } from '../content';

export const BUTIT_KULIT: Story = {
  id: 'butit-kulit',
  title: 'Butit Kulit',
  language: 'Filipino',
  lang: 'fil-PH',
  level: 'Beginner',
  minutes: 8,
  ages: '5–8 years',
  theme: 'Pagmamahal sa hayop',
  color: '#46B97B',
  cover: 'butit/cover.jpg',
  credit: 'Kuwento at iginuhit ni Joan D. Mayo · DepEd Manila',
  summary: 'Isang kuwento tungkol kay Butit, ang makulit pero malambing na aso ng isang batang mapagmahal sa hayop.',
  blurb:
    'Ang maikling kuwentong ito ay tungkol sa isang aso na makulit pero malambing na alaga ng isang batang mapagmahal sa hayop. ' +
    'Ipinakikita nito ang kahalagahan ng isang hayop sa buhay ng tao at ang halaga ng pagmamahal ng mga tao sa mga ito.',
  focus: { letter: 'Aa', sound: '/a/', example: 'dog', exampleFil: 'aso', syllables: ['a', 'so'], parts: ['d', 'o', 'g'], partsFil: ['a', 'so'] },
  pages: [
    {
      text: 'Ako ay may alaga noong ako ay bata pa. Mabalahibo siya. Puti at itim ang kulay niya. Makulit pero malambing siya. Siya ang aso kong si Butit. Mukhang busog lagi. Malaki ang kaniyang tiyan.',
      word: 'aso',
      image: 'butit/page-3.jpg',
      arTarget: 'butit-kulit-page-3',
      arModels: ['aso'],
    },
    {
      text: 'Tuwing umaga, pinaiinom ko siya ng gatas. Masiglang-masigla siya sa tuwing nakaiinom ng gatas. Dinidilaan niya ako sa pisngi.',
      word: 'gatas',
      image: 'butit/page-4.jpg',
      arTarget: 'butit-kulit-page-4',
      arModels: ['bata', 'aso'],
    },
    {
      text: 'Madalas kaming maglaro ng habulan at tagu-taguan sa aming bakuran.',
      word: 'maglaro',
      image: 'butit/page-5.jpg',
      arTarget: 'butit-kulit-page-5',
      arModels: ['bata', 'aso'],
    },
    {
      text: 'Sa tuwing ako ay papasok sa paaralan siya ay sumusunod sa akin. "Arf! Arf! Arf!", ang malakas na kahol niya sa akin na parang tinatawag ako at nagpapaalam pero hinahayaan ko lang ito.',
      word: 'paaralan',
      image: 'butit/page-6.jpg',
      arTarget: 'butit-kulit-page-6',
      arModels: ['bata', 'aso'],
    },
    {
      text: 'Minsan sa aking pag-uwi, masamang balita ang sumalubong sa akin. "Wala na si Butit, anak. Nabangga ito ng traysikel habang naglalakad ito pauwi nang sinundan ka", ang sabi ng aking Ina. Natulala ako at umagos ang luha sa aking mga mata.',
      word: 'Ina',
      image: 'butit/page-7.jpg',
      arTarget: 'butit-kulit-page-7',
      arModels: ['mama', 'bata'],
    },
    {
      text: 'Nakita ng aking Ina ang aking kalungkutan sa pagkawala ni Butit. "Hayaan mo anak, hihingi uli tayo ng aso sa Tito mo", ang pangungumbinsing sabi ni Inay. "Talaga Inay? Salamat po. Pangako po aalagaan ko na ito nang husto", ang masayang tugon ko sa aking Ina.',
      word: 'Inay',
      image: 'butit/page-8.jpg',
      arTarget: 'butit-kulit-page-8',
      arModels: ['mama', 'bata'],
    },
    {
      text: 'Kinaumagahan, nagising ako sa ingay sa labas. "Arf! Arf! Arf!", ang malakas na kahol ng isang aso.',
      word: 'nagising',
      image: 'butit/page-9.jpg',
      arTarget: 'butit-kulit-page-9',
      arModels: ['araw', 'bata'],
    },
    {
      text: 'Pagdungaw ko sa bintana, isang aso ang hawak ni Inay sa labas. Pinuntahan ko agad ito at niyakap. "Salamat Inay! Tatawagin ko uli siyang Butit".',
      word: 'Butit',
      image: 'butit/page-10.jpg',
      arTarget: 'butit-kulit-page-10',
      arModels: ['bata', 'aso'],
    },
  ],
  quiz: {
    question: 'Ano ang pangalan ng aso?',
    options: [
      { label: 'Butit', color: '#46B97B', correct: true },
      { label: 'Bantay', color: '#3E89C9' },
      { label: 'Puti', color: '#C49A6C' },
    ],
  },
  questions: [
    {
      question: 'Ano ang ipinaiinom ng bata kay Butit tuwing umaga?',
      options: [
        { label: 'Gatas', color: '#3E89C9', correct: true },
        { label: 'Tubig', color: '#46B97B' },
        { label: 'Juice', color: '#F6C85F' },
      ],
    },
    {
      question: 'Ano ang ginawa ni Inay para sa bata sa huli?',
      options: [
        { label: 'Binigyan siya ng bagong aso', color: '#46B97B', correct: true },
        { label: 'Binilhan siya ng pusa', color: '#E76F51' },
        { label: 'Isinama siya sa paaralan', color: '#9B8AFB' },
      ],
    },
  ],
};
