# Chapter 7: Designing a User Interface

In the previous chapter, you implemented authentication and authorization using Spring Security, which also includes all the sample e-commerce app application programming interfaces (APIs). In this chapter, you will develop the frontend of a sample e-commerce app using the React library. This user interface (UI) app will then consume the APIs developed in the previous chapter, Chapter 6, Security (Authorization and Authentication). This UI app will be a single-page application (SPA) that consists of interactive components such as Login, Product Listing, Product Detail, Cart, and Order Listing. This chapter will conclude the end-to-end development and communication between different layers of an online shopping app. By the end of the chapter, you will have learned about SPAs, UI component development using React, and consuming the REpresentational State Transfer (REST) APIs using the browser built-in Fetch API.

This chapter will cover the following topics:

- Learning React fundamentals
- Exploring React components and other features
- Designing e-commerce app components
- Consuming APIs using Fetch
- Implementing authentication


## Technical requirements

You need the following prerequisites for developing and executing the code:

- You should be familiar with JavaScript—data types, variables, functions, loops, and array methods such as map(), Promises, and async, and so on.
- Node.js 14.x with Node Package Manager (npm) 6.x (and optionally, yarn, which you can install using npm install yarn -g).
- Visual Studio Code (VS Code)—a free source code editor.
- React 17 libraries that will be included when you use create-react-app.

Let's get the ball rolling!

Please visit the following link to check the code: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter07/ecomm-ui

## Learning React fundamentals

React is a declarative library to build interactive and dynamic UIs, including isolated small components. Sometimes, it is also referred to as a framework because it is capable and comparable with other JavaScript frameworks, such as AngularJS. However, React is a library that works with other supported React libraries such as React Router, React Redux, and so on. You normally use it to develop SPAs, but it can also be used to develop full-stack applications.

React is used for building the view layer of the application, as per a Model-View-Controller (MVC) architecture. You can build reusable UI components with their own state. You can either use plain JavaScript with HyperText Markup Language (HTML) or JavaScript Syntax Extension (JSX) for templating. We'll be using JSX in this chapter. It uses a virtual Document Object Model (VDOM) for dynamic changes and interactions.

Let's create a new React app using the create-react-app utility. It scaffolds and provides the basic app structure that you'll use to develop the sample e-commerce app frontend.

### Creating a React app

You can configure and build a React UI app from scratch. However, React provides a create-react-app utility that bootstraps and builds a basic running sample app. You can further use it to build a full-fleshed UI application.

Its syntax is shown here:

```sh
npx create-react-app <app name>
```
npm package executor (NPX) is a tool that allows you to use command-line interface (CLI) tools and other executables available in the npm registry. It is by default available with npm 5.2.0, else you can install it using npm i npx. Therefore, it executes the create-react-app React package directly.

*USING NPM IN PLACE OF YARN*

By default, create-react-app uses the yarn package as a package manager. However, if you want, you can also use npm with the following command:
```sh
npx create-react-app ecomm-ui --use-npm
```
Now, let's create an ecomm-ui application using this command:

```sh
$ npx create-react-app ecomm-ui
Creating a new React app in C:\modern-api-with-spring-and-sprint-boot\Chapter07\ecomm-ui.
Installing packages. This might take a couple of minutes.
Installing react, react-dom, and react-scripts with cra-template...
yarn add v1.22.5
[1/4] Resolving packages...
[2/4] Fetching packages...
[3/4] Linking dependencies...
[4/4] Building fresh packages...
success Saved lockfile.
success Saved 297 new dependencies.
info Direct dependencies
├─ cra-template@1.1.1
├─ react-dom@17.0.1
├─ react-scripts@4.0.1
└─ react@17.0.1
info All dependencies
├─ @babel/compat-data@7.12.7
├─ @babel/core@7.12.10
├─ <Output truncated for brevity>
├─ yargs-parser@18.1.3
└─ yocto-queue@0.1.0
Done in 644.56s.
```

After it has installed all the required dependent packages, it continues by installing the template dependencies, as follows:

Installing template dependencies using yarnpkg...

```sh
yarn add v1.22.5
[1/4] Resolving packages...
[2/4] Fetching packages...
[3/4] Linking dependencies...
[4/4] Building fresh packages...
success Saved lockfile.
success Saved 15 new dependencies.
info Direct dependencies
├─ @testing-library/jest-dom@5.11.9
├─ @testing-library/react@11.2.3
├─ @testing-library/user-event@12.6.0
└─ web-vitals@0.2.4
info All dependencies
├─ @testing-library/dom@7.29.4
├─ @testing-library/jest-dom@5.11.9
├─ <Output truncated for brevity>
├─ strip-indent@3.0.0
└─ web-vitals@0.2.4
Done in 109.90s.
```

It may also ask you to add some testing dependencies— you can install these using the following command:
```sh
$ yarnpkg add @testing-library/jest-dom@^5.11.4 @testing-library/react@^11.1.0 @testing-library/user-event@^12.1.10 web-vitals@^0.2.4
```

Once it is installed successfully, you can go to the app directory and start the application installed using create-react-app by running the following code:
```
$ cd ecomm-ui
$ code .
```
The code . command opens the ecomm-ui app project in VS Code. You can then use the following command to start the development server:
```
$ yarn start
```
Once the server has started successfully, it will open a new tab on your default browser with localhost:3000, as shown in the following screenshot:

Figure 7.1 – Default UI app created by the create-react-app utility

Our bootstrapped React UI is up and running, but you now need to understand the basic concepts and files generated by create-react-app before you build an e-commerce UI app on top of it.

### Exploring basic structures and files
A scaffolded React app contains the following directories and files inside the root project directory:

Let's understand the main parts, as follows:

node_modules: You don't make any changes here. Node-based applications keep a local copy of all the dependent packages here.

- public: This directory contains all the static assets of an app here, including index.html, images, favicon icon, and robots.txt.

- src: This directory contains all the dynamic code, including React code and Cascading Style Sheets (CSS) (including Synthetically Awesome Style Sheets (Sass), Leaner Style Sheets (Less), and so on). It also contains the test code.

- package.json: This JavaScript Object Notation (JSON) file contains all the metadata, commands (inside scripts), and dependent packages (inside dependencies and dev-dependencies).

You can remove the serviceWorker.js file (if generated), the logo.svg file, and test files from the src directory for now as we are not going to use them in this chapter.

Let's understand the package.json file in the next subsection.

### Understanding the package.json file
You can also view the package.json file that contains all the dependencies under the dependencies and dev-dependencies fields. It is similar in nature to the build.gradle file.

The main React libraries are react and react-dom, mentioned in the dependencies field; these are for React and the virtual DOM respectively.

package.json also contains a script field that contains all the commands you can execute on this application. We have used the yarn start command to start the application in development mode. Similarly, you can execute other commands, as shown in the following code block, with yarn and npm:

react-scripts is a CLI package installed by the create-react-app utility. It contains many dependencies, and few of the primary dependencies are listed here:

- Webpack (https://webpack.js.org/): This is a module bundler that bundles JavaScript, CSS, images, HTML, and so on. CSS and images may require extra loaders as dependencies. For example, it will pick all JavaScript files and bundle them into a single JavaScript file, though you can customize the way it bundles them by using a webpack.config.js configuration.
- Jest (https://jestjs.io/): Jest is a JavaScript testing framework maintained by Facebook.
- ESLint (https://eslint.org/): ESLint is a linter that allows you to maintain code quality. It is very similar to Checkstyle in the Java world.
- Babel (https://babeljs.io/): Babel is a JavaScript transcompiler tool that converts JavaScript code to backward-compatible JavaScript code. The latest JavaScript draft version is ECMAScript 2020, which is also referred as ES10. The latest JavaScript stable version is ECMAScript 2018 (ES9). Babel allows you to generate optimized backward-compatible code from JavaScript code written using the latest versions.

You can find react-scripts under the dependencies field in package.json. Let's understand each of these commands, as follows:

- start: This command allows you to start the development server in a node environment. It also provides the hot reload feature, which means any changes to the React code would be reflected in the application, without a restart being required. Therefore, if there are any linting or code issues, this would show up accordingly in the console (terminal window) and web browser.

- build: This command packages the React application code for production deployment. It does the bundling of the JavaScript files in one CSS file into another and also minifies and optimizes the code files. You can then use this bundle to deploy on any web server.

- test: This command executes a test using the test runner (Jest tool). It executes all test files having extensions such as .test.js or .spec.js.

- eject: React comes with default build configurations such as webpack, Babel, and so on. The build configuration has the best practices implemented for optimizing the built app. This command helps you to eject the hidden configuration, after which you can override and customize the build configuration. However, you should do this with the utmost care because this is a one-way activity and you can't reverse it.

Let's understand how React works, in the next subsection.


### Understanding how React works

A web page is nothing but an HTML document. HTML documents contain the DOM, a tree-like structure of HTML elements. Any changes to the DOM are reflected in the rendering of the HTML document in the browser. Making changes in the actual DOM— and, specifically to the nth level—is a heavy operation in terms of traversal and rendering the DOM, because each change is done on the whole DOM, and this is a time-and memory-consuming operation.

React uses a VDOM to make these operations lightweight. A VDOM is an in-memory copy of the actual DOM. React maintains the VDOM using the react-dom package. Therefore, when you initialize the React app, you first pass the root HTML element ID to the ReactDOM object's render function. React writes the VDOM under this root element after its first render.

After the first render, only the necessary changes are written to the actual DOM based on changes to React components and their state. The React components' render function returns the markup in JSX syntax. Then, React transforms it to HTML markup and compares the generated VDOM with the actual HTML DOM, and only makes the necessary changes to the actual DOM. This process then continues till the components get changed. Let's explore how the first render takes place.

### Bootstrapping of the React app
The index.html file under the public directory contains the main HTML file. It's an application skeleton that contains the site title, meta elements, a body element, and a div element under the body with an ID of root. You pass this root element to the render function of ReactDOM in index.js, in the src directory. This is the entry point of the React app. Let's have a look at its code, as follows:

```javascript
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
ReactDOM.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
  document.getElementById('root')
);
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/index.js

Here, React uses the ReactDOM object from the react-dom package to render the page. The render() function contains two arguments: element and container. It can also have a third (optional) argument for the callback.

You are passing an `<App />` tag component wrapped with React's strict mode component as an element argument and a <div id= "root"> inside the <body> element of index.html as a container argument to the render function.

App components can be a single component or a parent component with single or multilayer child components. A single component won't contain any other React component; it would simply contain the JSX, and that's it. However, parent components may contain one or more child component, and those child components may contain one or more child components, and so on. For example, an App component may have header, footer, and content components. A content component may have a cart component, and then the cart component may have items inside it.

A `<React.StrictMode>` component is a special React component that gets rendered twice in development mode to check the best practices, deprecated methods, and potential risk in your React components, and prints warnings and suggestions in the console log. It has no impact on production build because it works only in development mode.

The `render` function transforms the JSX of the app component to HTML and adds it inside the <div id="root"> tag, then it compares the VDOM with the real DOM and makes the necessary changes in the real DOM. This is how React components get rendered on the browser.

You now understand that React components are key here. Let's deep dive into them in the next section.



## Exploring React components and other features

Each page is built up using React components—for example, the Product Listing page of Amazon can broadly be divided into Header, Footer, Content, Product List, Filter and Sorting options, and Product Card components. You can create components in React in two ways: by using JavaScript classes or by using functions.

Let's create a sample header component in React with both a function and a class.

You can either write a plain old JavaScript function or ES6 arrow functions. We'll mostly use arrow functions. In the following code snippet, check out the Header component using a JavaScript arrow function:

```js
export const Header = (props) => {
  return (
    <div>
      <h1>{props.title}</h1>
    <div>
  )
}
```
Let's create the same Header component using a JavaScript class, as follows:

```js
export default class Header extends React.Component {
  render() {
    return (
      <div>
        <h1>{this.props.title}</h1>
      <div>
    )
  }
}
```

Let's understand both of these functions point by point, as follows:

- Both are returning the JSX that looks similar to HTML, which actually gets rendered after transformation.

- Both are exporting the function and class respectively so that they can be imported by other components.

- Both are having props—one as an argument and one bound with a this scope, which is part of React.Component. Props represent the attributes and their values—for example, here, a title attribute is used. When it gets rendered, it is replaced by the title attribute's value.

- The class needs a render() function, whereas the function simply needs a return statement.

Let's see how the Header component could be used. You can use this Header component as you would use any other HTML tag in your JSX code, as shown next:

```html
<Header title="Sample Ecommerce App" />
```
When this Header component gets rendered, it will show the title wrapped in an <H1> element.

Let's explore the JSX next. This is how you use the props: you add an attribute (such as title) to its value while using the component. Inside the component, you can access these attributes (properties) by using props directly or using the { title } destructuring form in functional components and by using this.props in class components.


#### Exploring JSX

React components would return the JSX. You can write HTML code to design the components because JSX is very similar to HTML, except for the HTML attributes. Therefore, you need to make sure to update attributes such as class to className, for to htmlFor, fill-rule to fillRule, and so on. The advantage of using the React.StrictMode component is that you get a warning and a suggestion to use the correct JSX attribute names if you use HTML attributes or have a typo.

You can also put any JavaScript expressions inside JSX or an element's attributes to make the component dynamic by using the expression wrapped in curly braces ({}).

Let's have a look at some sample code to understand both JSX and expressions. The following JSX code snippet has been taken from the CartItem component. Check out the highlighted code for expressions; the rest of the code is JSX, which is very similar to HTML:

```html
<div className="w-32">
   <img className="h-24" src={item?.imageUrl} alt="" />
</div>

<div className="flex flex-col justify-between ml-4 flex-grow">
   <Link to={"/products/" + item.id} className="font-bold
        text-sm text-indigo-500 hover:text-indigo-700">
      {item?.name}
   </Link>
   <span className="text-xs">Author: {author}</span>
   <button className="font-semibold hover:text-red-500
       text-indigo-500 text-xs text-left" onClick={() =>
       removeItem(item.id)}>
      Remove
   </button>
</div>
```
The preceding code fragment represents a cart item that shows the product image, product name, author, and Remove button. The product name is also a link that links to the product detail page. You can design using JSX (read HTML) as shown. Please also note that the class attribute name is changed to className because it is a JSX. Link is a part of the react-router-dom library.

You are done with the cart item's design part. Now, you need a mechanism to populate the values and add the event handling in it. This is where a JSX expression helps you.

You use item—an object that represents the cart item, and author—a variable that contains the author name. Both are part of the React component's state. You will learn more about the state in the next subsection, but for the time being you can think of them as variables defined in the CartItem component. Once you write the JSX (read HTML), dynamic values (from variables) and interaction (for events) can be defined using the expressions wrapped inside curly braces ({}).

Let's understand each of the expressions next, as follows:

- src={item?.imageUrl}: You get the item (product) image Uniform Resource Locator (URL) as part of the API response. You simply assign it to the src attribute of the img tag. Note that the dot operator (.) allows you to access the property of an object. The code may throw an error if you try to read the property of any null or undefined object. You can avoid that by using the ?. operator. Then, the property (in this case, imageUrl) would only be read if an object (in this case, item) is not null or undefined.

- to={"/products/" + item.id}: Here, links to an attribute are formed by using the object item's id property.
{item?.name}: Here, the name of the product is displayed using the name property of the item object.

- Author: {author}: The author value is displayed using the author variable.

- onClick={() => removeItem(item.id)}: This is the way you associate a user-defined function with an event. Here, removeItem() would be called by passing the item object's id property on the click of a button. If you are not passing any argument or using multiple statements, then you can directly pass the function name instead of using the arrow function—for example, onClick={removeItem}.

Next, we will deep dive into the state of React components. Let's see how this works.

### Exploring a component's state

Components are dynamic and contain a state. The state represents the data and metadata held by the component at a given point in time. There are two levels of state: a global (app-level) state and a local (component-level) state.

Earlier (prior to React version 16.8), the state was only supported in components defined using classes. Now, React supports the state in both functional and class components. React supports the state in functional components using hooks such as useState(), useContext(), and so on.

React introduced hooks (a set of functions) in the 16.8 version, which introduced many features to the functional component that were earlier not supported, such as state and an event similar to componentDidMount (a lifecycle method in the class that indicates a component was mounted), and you can now perform certain operations such as loading data using APIs, and so on.

Let's understand the React hooks next.

### Hooks

Hooks are special React functions that are provided in React version 16.8 onward. Each hook represents a special feature that you can use in functional components. Let's understand the most popular and common hooks one by one, as follows:

- **useState**: useState allows you to define and maintain the state. Let's see how you can use this hook. First, you import the useState hook at the top of the component code file, as follows:
```js
  import {useState} from "react";
```
Next, inside your component's arrow function code, define the state before the return statement, as shown next:
```js
const [total, setTotal] = useState(0);
```

You need to define both state and state setter functions in an array while declaring the state. Here, the total state is defined with its setter function. You can use any type of state, such as an object, array, string, or number. The total state is of type number, therefore it is initialized with 0. setTotal is a setter function. The setter function allows you to update the state (total here)—for example, you can update the total state by calling setTotal(100), then the total state would be changed from 0 to 100.

React tracks the state's setter function and whenever it is called, React updates the state of the component and re-renders the component. The naming convention of the setter function is to prefix the state name with set and make the state's first letter a capital letter. Therefore, we have used the setTotal name for the total state. You'll use useState for local state management in most components.

- **useEffect**: You use a useEffect() hook when you want to do something after rendering a component. This gets called after each render. You can also use it when you want to load the initial data from an API or add an event listener. However, if an API call should be made once, then you can pass the empty array ([]) dependency while calling it. You'll find multiple instances of useEffect in ecomm-ui code when an empty array is passed for a single call.

React recommends using multiple useEffect functions inside components for separating the concern. Also, make sure it returns an arrow function for cleanup. For example, when you add the event listener for any component, it should return an arrow function that removes the event listener.

- **useContext**: You can pass props from one component to another. Sometimes, you have to use props drilling to the n-th level. React also provides an alternative way to define these props so that they can be used in any component in a tree without using prop drilling. You would use it for props that are common across components, such as theme or isUserLoggedIn.

React provides a `createContext()` function to create a context. It returns a provider and consumer to provide access to its values and changes respectively (see the next code block). However, useContext can easily make use of the context by removing usage of the consumer. 

The following code snippet depicts useContext usage:

```js
const LoggedInContext = createContext();
const App = () => {
   return (
      <LoggedInContext.Provider isUserLoggedIn>
         <ProductList/>
      <LoggedInContext.Provider/>
   );
}

const ProductList = () => {
   return (
      <LoggedInContext.Consumer> 
        { 
          (isUserLoggedIn) =>
            <div>Is user logged-in: {isUserLoggedIn}</div>
        } 
      <LoggedInContext.Consumer>
   );
}
ReactDOM.render(<App/>, document.getElementById("root"));
```
You can simplify the ProductList component's return block in the previous code snippet (check the highlighted code) with useContext, as follows:

```js
const LoggedInContext = createContext();

const App = () => {
   return (
      <LoggedInContext.Provider isUserLoggedIn>
         <ProductList/>
      <LoggedInContext.Provider/>
   );
}

const ProductList = () => {
   const isUserLoggedIn = useContext(LoggedInContext);   
   return (
      <div>Is user logged-in: {isUserLoggedIn}</div>
   );
}
ReactDOM.render(<App/>, document.getElementById("root"));
```
This is how you can use `createContext` and `useContext` hooks.

- **useReducer**: This is an advanced version of the useState hook that not only allows you to use a component's state but also provides better controls to manage its state by taking the reducer function as a first argument. It takes the initial state as a second argument. Check out its syntax, as seen in the following code block:

```js
const [state, dispatch] = useReducer(reducer, initialState);
```

The reducer function is a special function that takes state and action as arguments and returns a new state. We'll explore this more when we build the CartContext component later in this chapter.

Now that you have learned the basic concepts of React, let's add some styling to the ecomm-ui application using TailwindCSS.

### Styling components using Tailwind

Tailwind CSS is a utility CSS framework that helps you to design a responsive UI. It supports theming, animation, pre-defined padding and margins, flex, grids, and so on. You can install Tailwind and its peer packages using yarn, as shown in the following code snippet (executing it from the project root directory):

```sh
$ yarn add -D tailwindcss@npm:@tailwindcss/postcss7-compat @tailwindcss/postcss7-compat postcss@^7 autoprefixer@^9
```

`create-react-app` doesn't support PostCSS 8 at the time of writing this chapter, so you need to install the Tailwind CSS v2.0 with PostCSS 7 compatibility build for now, as shown in the previous code snippet. However, it can be changed to the appropriate version of PostCSS once `create-react-app` starts supporting it (version 8+).

Configuring the Tailwind build needs the Create React App Configuration Override (CRACO) package.

### Installing and configuring CRACO

You also need to install CRACO to be able to configure the Tailwind build because `create-react-app` doesn't let you override the PostCSS configuration natively. CRACO allows you to override the configuration created by create-react-app. Let's install it by executing the following command from the project root directory:
```
$ yarn add -D @craco/craco
```
Once installation is done successfully, you can update scripts in the package.json file to replace react-scripts with craco for all scripts except eject, as follows:
```json
  {
    ...
    ...
    "scripts": {
     "start": "craco start",
     "build": "craco build",
     "test": "craco test",
     "eject": "react-scripts eject"
    },
  }
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/package.json

Next, create a file named `craco.config.js` at the root of the project and add tailwindcss and autoprefixer as `postcss` plugins, as follows:

```json
module.exports = {
  style: {
    postcss: {
      plugins: [
        require('tailwindcss'),
        require('autoprefixer'),
      ],
    },
  },
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/craco.config.js

Now, you can create a Tailwind configuration file.

### Creating a Tailwind configuration file
You can create and initialize a `tailwind.config.js` file using the following command:
```
$ npx tailwindcss init
```
This will create a default `tailwind.config.js` file at the root of the project with minimal configuration, as illustrated in the following code snippet:

```json
module.exports = {
  purge: [],
  darkMode: false, // or 'media' or 'class'
  theme: {
    extend: {},
  },
  variants: {
    extend: {},
  },
  plugins: [],
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/tailwind.config.js

Now, we can add configuration to purge unused styles in production.



## 미사용 스타일을 제거하기 위한 설정

You would like to keep the style sheet size down in a production environment because this improves the performance of the application. You can purge unnecessary styles by adding the following purge block in the tailwind.config.js file. Then, Tailwind can tree-shake unused styles while building the production build. You can set the PURGE_CSS environment variable to production for production builds. 

The code is illustrated in the following snippet:

```json
  module.exports = {
    purge: {
     enabled: process.env.PURGE_CSS === "production" ? true
     : false,
     content: ["./src/**/*.{js,jsx,ts,tsx}",
     "./public/index.html"],
    },
    darkMode: false, // or 'media' or 'class'
    theme: {
      extend: {},
    },
    variants: {
      extend: {},
    },
    plugins: [],
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/tailwind.config.js

Next, we will add Tailwind to React.

### Including Tailwind in React

Open the `src/index.css` file that create-react-app generates for you by default and import Tailwind's base, components, and utilities styles, replacing the original file contents, as follows:

```js
@import "tailwindcss/base";
@import "tailwindcss/components";
@import "tailwindcss/utilities";
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/index.css

These statements import the styles generated by the build based on the Tailwind configuration when you execute the build.

Finally, make sure that the CSS file is being imported in the src/index.js file by running the following code:

```js
  import './index.css';
  import App from './App';
  import reportWebVitals from './reportWebVitals';

  ReactDOM.render(
    <React.StrictMode>
      <App />
    </React.StrictMode>,
    document.getElementById('root')
  );

  ...
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/index.js

Done! Next, you can execute yarn run start to use Tailwind CSS in the `ecomm-ui` app.

### Adding basic components

First, remove the following files created by create-react-app:

- App.css
- logo.svg

Don't forget to remove these file references from /src/App.js too.

Then, create a new components directory under /src. You will create all new components under this directory, as shown in Figure 7.2. Let's create three new components, as follows:

- **Header**: 엡 이름, 로그인 등 헤더 항목을 화면 상단에 표시

- **Container**: 상품 목록과 같은 주 내용을 표시

- **Footer**: 카파라이트와 같은 푸터 항목을 하단에 표시.

The basic structure can be seen in the following screenshot:


Figure 7.2 – Basic structure of app containing Header, Footer, and Container components

Let's add these containers. First, we'll create a Header component, as shown in the following code snippet:
```js
const Header = () => {
  return (
    <div>
      <header className="p-2 border-b-2 border-gray-300 bg-gray-200">
        <h1 className="text-lg font-bold">Ecommerce App</h1>
      </header>
    </div>
  );
};
export default Header;
```

Similarly, you can create a Footer component, as shown in the following code snippet:

```js
const Footer = () => {
  return (
    <div>
      <footer className="text-center p-2 border-t-2 bg-
        gray-200 border-gray-300 text-sm">
        No &copy; by Ecommerce App.{" "}
        <a href="https://github.com/PacktPublishing/Modern-
            API-Development-with-Spring-and-Spring-Boot">
          Modern API development with Spring and Spring Boot
        </a>
      </footer>
    </div>
  );
};

export default Footer;
```
Similarly, you can create a Container component, as shown in the following code snippet:

```js
const Container = () => {
  return (
    <div className="flex-grow flex-shrink-0 p-4">
      <p>Hello, text/element would appear in container</p>
    </div>
  );
};
export default Container;
```

And finally, you can modify the /src/App.js file, as shown in the following code snippet:

```js
import Header from "./components/Header";
import Footer from "./components/Footer";
import Container from "./components/Container";

function App() {
  return (
    <div className="flex flex-col min-h-screen h-full">
      <Header />
      <Container />
      <Footer />
    </div>
  );
}
export default App;
```
This is how you can create and use new components. These components are in their simplest form and are kept as such to understand these more easily. However, you can find refined and improved versions of these components on GitHub, as follows:

- **Header** component source: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Header.js

- **Footer** component source: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Footer.js

- **Container** component (which contains the actual content in the center) would be replaced with the component switch from react-router-dom that would display the components based on a given route, such as cart, orders, and login.

Now, you can start the actual ecomm-ui development.


## Designing e-commerce app components

Design is not only a key part of the user experience (UX)/a UI, but is also important for frontend developers. Based on the design, you can create reusable and maintainable components. However, a sample e-commerce app is a simple application that does not need much attention. You will create the following components in this application:

- **Product listing component**: A component that displays all the products and also acts as a home page. Each product in the listing will be displayed as a card with the product name, price, and two buttons—Buy now and Add to bag. The following screenshot displays the Product listing page, which shows product information along with an image of the product:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_7.3_B16561.jpg)

Figure 7.3 – Product listing page (home page)

- **Product detail component**: This is a component that displays details of the clicked product. It displays the product image, product name, product description, tags, and Buy now and Add to bag buttons, as shown next:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_7.4_B16561.jpg)
Figure 7.4 – Product detail page

- **Login component**: Login components allow a user to log in to an app by using their username and password, as illustrated in the following screenshot. It displays an error message when a login attempt fails. Click on Cancel to go back to the to the Product listing page. The Product listing page shows a list of products a customer can buy:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_7.5_B16561.jpg)
Figure 7.5 – Login page

- **Cart component**: A Cart component lists all the items that have been added to the cart. Each item displays product image, name, description, price, quantity, and total. It also provides a button to decrease and increase quantity, and a button to remove an item from the cart.

Product name is a link that takes the user back to the Product detail page. The Continue shopping button takes the user to the Product listing page. The `CHECKOUT` button performs the checkout. On successful checkout, an order is generated and the user is redirected to the Orders page, as shown next:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_7.6_B16561.jpg)

Figure 7.6 – Cart page

- **Orders component**: The Orders page shows all orders placed by the user in a tabular form. The Orders table displays the order date, ordered items, order status, and order amount for each order.

The order date will be displayed in the user's local time, but on the server it will be in Universal Coordinated Time (UTC) format. Order items would be displayed in an order list, with their quantity and unit price in brackets, as illustrated in the following screenshot:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_7.7_B16561.jpg)

Figure 7.7 – Order page

Let's start coding these components. First, you will code the Product listing page, which fetches the products from the backend server using the REST API.


## Fetch를 사용하여 API 사용

Let's create the first component—that is, the `Product Listing Page`. Create a new file in the src/components directory with the name `ProductList.js`. This is the parent component of the Product Listing page.

이 컴포넌트는 백엔드 서버에서 상품들을 조회하고 자식 컴포넌트인 Products에게 제공한다

`Products` contain the logic of fetched product list iterations. Each iteration renders the card UI for each product.

`ProductCard` is another component, therefore you'll create another file, `ProductCard.js`. You can write the product card logic inside products, but to single out the responsibility it's better to create a new component.

The `ProductCard` component has a `Buy now` button and an `Add to bag` link. These links should only work if the user is logged in, else it should redirect the user to the login page.

You now have an idea about the `Product Listing Page` component tree structure. Now, our first task is to have an API client that fetches products we can render in these components.

### product API 클라이언트 작성

You are going to use the Fetch browser library as a REST API client. You can also use a third-party library such as `axios`. However, this means you need to include another dependency. When you can do the same job using a built-in browser API, why include extra dependencies?

You'll create a configuration file for all API clients to use. Let's name it `Config.js` so that you can create under the src/api directory.

`Config` is a class that contains constants such as URLs and common methods such as `DefaultHeaders()` and `tokenExpired()`:

```js
class Config {
  SCHEME = process.env.SCHEME ? process.env.SCHEME : "http";
  HOST = process.env.HOST ? process.env.HOST : "localhost";
  PORT = process.env.PORT ? process.env.PORT : "8080";
  LOGIN_URL = `${this.SCHEME}://${this.HOST}:${this.PORT}/api/v1/auth/token`;
  PRODUCT_URL = `${this.SCHEME}://${this.HOST}:${this.PORT}/api/v1/products`;

  // other constants removed for brevity
  defaultHeaders() {
    return { "Content-Type": "application/json", Accept: "application/json",
    };
  }
  headersWithAuthorization() {
    return {...this.defaultHeaders(),
      Authorization: localStorage.getItem(this.ACCESS_TOKEN),
    };
  }
// continue…
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/api/Config.js

defaultHeaders() 함수는 모든 API 호출에서 사용되는 공통 헤더를 반환하고 headersWithAuthorization()은 Authorization 헤더와 함께 공통 헤더를 반환합니다. headersWithAuthorization()은 기본 헤더를 검색하기 위해 객체 분해를 사용합니다. Authorization 헤더는 사용자가 성공적으로 로그인할 때 설정되고 사용자가 로그아웃하면 제거되는 로컬 저장소에서 가져옵니다.

또한 단순히 로컬 저장소에 저장된 토큰의 만료 시간을 확인하는 tokenExpired() 함수도 있습니다. 이 만료 시간은 액세스 토큰(JSON 웹 토큰 또는 JWT)에서 추출됩니다. 만료 시간이 현재 시간을 초과하면 true를 반환합니다. 

다음 코드에서 이 함수의 코드를 확인하세요.

```js
// Config.js continue
  tokenExpired() {
   const expDate = Number(localStorage.getItem(this.EXPIRATION));
   return (expDate <= Date.now()) 
  }

  storeAccessToken(token) {
   localStorage.setItem(this.ACCESS_TOKEN, `Bearer ${token}`);
   localStorage.setItem(this.EXPIRATION, this.getExpiration(token));
  }

  getExpiration(token) {
   let encodedPayload = token ? token.split(".")[1] : null;
   if (encodedPayload) {
     encodedPayload = encodedPayload.replace(/-/g,"+").replace(/_/g, "/");
     const payload = JSON.parse(window.atob(encodedPayload));
     return payload?.exp ? payload?.exp * 1000 : 0;
   }
   return 0;
  }
}
```

Config 클래스에는 단순히 로컬 저장소에 액세스 토큰과 만료 시간을 저장하는 storeAccessToken() 함수도 있습니다. getExpiration() 함수는 액세스 토큰에서 만료 시간을 추출합니다. 이 함수는 먼저 토큰 문자열에서 페이로드를 추출한 다음 디코딩하고 JSON으로 변환합니다. 마지막에 페이로드가 유효한 객체이면 만료 시간을 반환하고 그렇지 않으면 0을 반환합니다.

이제 다음 코드 블록과 같이 ProductClient.js 파일에서 이 Config 클래스를 사용하겠습니다.

```js
import Config from "./Config";

class ProductClient {
  constructor() { 
    this.config = new Config(); 
  }
  
  async fetchList() {
    return fetch(this.config.PRODUCT_URL, {
      method: "GET",
      mode: "cors",
      headers: {
        ...this.config.defaultHeaders(),
      },
    })
    .then((response) => Promise.all([response, response.json()]))
    .then(([response, json]) => {
      if (!response.ok) {
        return { success: false, error: json };
      }
      return { success: true, data: json };
    })
    .catch((e) => { return this.handleError(e); });
  }

// continue…
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/api/ProductClient.js

ProductClient 클래스의 생성자에서 Config가 인스턴스화 됩니다. 이 클래스에는 제품 가져오기를 위한 두 가지 비동기 함수인 fetchList() 및 fetch()가 포함되어 있습니다. 전자는 모든 제품을 가져오고 후자는 해당 ID를 기반으로 단일 제품을 가져오는 것입니다. fetchList()는 브라우저 fetch() 함수로 제품 목록을 가져옵니다. URL을 첫 번째 인수 입력으로 전달하고 HTTP 메서드, 모드 및 헤더를 두 번째 인수로 포함하는 초기화 객체를 요청합니다. 

fetch 브라우저 호출은 요청을 처리하는 데 사용하는 Promise를 반환합니다. 먼저 응답과 응답 JSON에 대한 Promise를 resolve한 다음 response.ok가 참인지 아닌지 확인합니다. response.ok는 200~299 범위의 상태에 대해 true를 반환합니다. 응답이 성공하면 data 및 success 필드가 있는 개체를 true로 반환합니다. 응답이 실패하면 success를 false로 반환하고 data 필드에 오류 응답을 표시합니다.

마찬가지로 ID별로 제품을 검색하는 함수를 작성할 수 있습니다. 다음 코드 블록에서 볼 수 있듯이 URL을 제외하고는 모든 것이 동일합니다.

```js
  // ProductClient.js continue…
  async fetch(prodId) {
    return fetch(this.config.PRODUCT_URL + "/" + prodId, {
      method: "GET",
      mode: "cors",
      headers: {
        ...this.config.defaultHeaders(),
      },
    })
      .then((response) => Promise.all([response, response.json()]))
      .then(([response, json]) => {
        if (!response.ok) {
          return { success: false, error: json };
        }
        return { success: true, data: json };
      })
      .catch((e) => { this.handleError(e); });
  }

  handleError(error) {
    const err = new Map([
      [TypeError, "There was a problem fetching the response."],
      [SyntaxError, "There was a problem parsing the response."],
      [Error, error.message],
    ]).get(error.constructor);

    console.log(err);
    return err;
  }
}

export default ProductClient;
```

The handleError() function checks the type of the error (using error.constructor) and, based on that, returns the appropriate error message.

Please note that other API clients such as `CartClient`, `CustomerClient`, and `OrderClient` are developed in a similar fashion. 

The code is available at the following locations:

- **CartClient**: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/api/CartClient.js

- **CustomerClient**: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/api/CustomerClient.js

- **OrderClient**: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/api/OrderClient.js

Now, we can use ProductClient to fetch the products. Let's code the ProductList component and its child components.

### Product Listing 페이지 코딩하기

`ProductList` is a straightforward component that loads the products after their first render using `ProductClient`. You know that for this purpose, `useEffect` hooks should be used.

```js
import CartClient from "../api/CartClient";
import ProductClient from "../api/ProductClient";
import { updateCart, useCartContext } from "../hooks/CartContext";
import Products from "./Products";

const ProductList = ({ auth }) => {
  const [productList, setProductList] = useState();
  const [noRecMsg, setNoRecMsg] = useState("Loading...");
  const { dispatch } = useCartContext();

  useEffect(() => {
    async function fetchProducts() {
      const res = await new ProductClient().fetchList();
      if (res && res.success) {
        setProductList(res.data);
      } else {
        setNoRecMsg(res);
      }
    }

    async function fetchCart(auth) {
      const res = await new CartClient(auth).fetch();

      if (res && res.success) {
        console.log(res.data);
        dispatch(updateCart(res.data.items));

        if (res.data?.items && res.data.items?.length < 1) {
          setNoRecMsg("Cart is empty.");
        }
      } else {
        setNoRecMsg(res && typeof res === "string" ? res : res?.error?.message);
      }
    }
    if (auth?.token) fetchCart(auth);
    fetchProducts();
  }, []);

  // Continue…
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/ProductList.js

The ProductList component uses auth as a prop. It contains authentication information such as a token. The ProductList component is used as the main App component, and auth is passed to the ProductList component by it.

Please note that you have passed an empty array ([]) as a dependency to make sure that the API is called only once. You are using a useState hook to store the product list (productList) and message states (noRecMsg—no record) by using setter methods.

### ProductList에서 cart를 조회하는 이유

ProductList 구성 요소 및 해당 하위 구성 요소는 인증되지 않은 사용자가 사용할 수 있습니다. 사용자가 지금 구매 버튼이나 장바구니에 추가 링크를 클릭하면 로그인하라는 메시지가 표시됩니다. 로그인하면 사용자가 장바구니에 항목을 추가할 수 있습니다. 사용자가 이미 장바구니에 일부 항목을 가지고 있을 수 있습니다. 따라서 장바구니에 상품을 추가할 때 기존 상품의 수량을 늘려야 하며, 클릭한 상품이 장바구니에 없으면 장바구니에 추가해야 합니다.

Cart는 완전히 별도의 구성 요소입니다. 이는 App 구성 요소에서 Cart 및 ProductCard 구성 요소로 카트 소품 드릴을 수행하거나 카트에 대한 useContext 후크가 없으면 카트에 액세스할 수 없음을 의미합니다. Redux와 매우 유사한 cart 상태를 유지하기 위해 사용자 지정 저장소를 구축했습니다. 이 장의 뒷부분에서 이 라이브러리에 대해 자세히 알아볼 것입니다. Dispatch는 백엔드 서버에서 받은 cart 항목을 cart 컨텍스트로 업데이트하는 작업입니다.

다음으로 JSX 템플릿을 만들고 가져온 productList 구성 요소를 추가 렌더링을 위해 하위 구성 요소인 Products에 전달합니다.

```js
  // ProductList.js continue…
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      {productList ? (
        <div className="flex flex-wrap -mx-1 lg:-mx-4">
          <Products auth={auth} productList={productList ?  productList : []} />
        </div>
      ) : (
        <div className="text-lg font-semibold">{noRecMsg}</div>
      )}
    </div>
  );
};

export default ProductList;
```

Here, it also passes the auth object as a prop to Products.
Let's have a look at the Products code, as follows:

```js
import ProductCard from "./ProductCard";

const Products = ({ auth, productList }) => {
  return (
    <>
      {productList.map((item) => (
        <ProductCard key={item.id} product={item} auth={auth} />
      ))}
    </>
  );
};

export default Products;
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Products.js

단순히 ProductList 구성 요소가 전달한 제품 목록을 반복하는 작업을 수행하고 제품 소품이 있는 각 항목을 auth 개체와 함께 ProductCard 구성 요소에 전달합니다.

다음과 같이 두 가지 React 개념의 사용법을 관찰할 수 있습니다.

컬렉션을 기반으로 구성 요소를 생성할 때 React는 고유하게 식별하기 위해 key 인덱스가 필요합니다. 이렇게 하면 React가 변경, 제거 또는 추가된 항목을 식별할 수 있습니다. 여기서는 아이템 ID를 사용했습니다. 컬렉션에 ID가 없는 경우 다음 코드 예제와 같이 인덱스를 사용할 수도 있습니다.
```js
{productList.map((item, index) => (
   <ProductCard key={index} product={item} auth={auth} />
))}
```

Now, let's have a look at the last child component of the ProductList component: ProductCard. The ProductCard component simply passes Product values to JSX template expressions for rendering.

We have added some extra code to add the functionality associated with Add to bag and Buy now click events.

## 라우팅 설정

라우팅은 단일 페이지에 대한 라우팅을 제공하는 메커니즘입니다. 즉, 새 페이지마다 브라우저 URL이 변경 사항을 반영하고 페이지를 북마크할 수 있습니다. 또한 URL 기록을 유지 관리합니다. 라우팅 관리를 위해 react-router-dom 패키지를 사용할 것입니다. 다음 코드 스니펫과 같이 라우팅을 사용하려면 react-router-dom 패키지를 추가해야 합니다. 프로젝트 루트 디렉터리에서 실행해야 합니다.

```sh
$ yarn 추가 react-router-dom
```

ecomm-ui 애플리케이션의 루트 구성요소이기 때문에 App 구성요소에서 라우팅을 구성할 것입니다. ProductList 구성 요소에서 react-router-dom 패키지의 Link 구성 요소와 useHistory() 후크를 사용할 것입니다.

다음과 같이 이해합시다.

- Link: <a> HTML 앵커 태그와 유사합니다. href 속성 대신 to 속성을 사용하여 URL을 연결합니다. 경로 라이브러리는 링크를 유지 관리하므로 링크를 클릭할 때 to 속성과 함께 링크가 전달될 때 렌더링할 구성 요소를 알고 있습니다.

- useHistory(): 구성 요소 내부 탐색을 허용하고 라우터 상태에 액세스합니다. ProductList 구성 요소의 checkLogin() 함수에서 볼 수 있듯이, 탐색을 위해 push("/path ") 함수를 사용합니다.
다음 제품 기반 구성 요소인 ProductCard의 개발을 계속해 보겠습니다.

### ProductCard 컴포넌트 개발

First, you import the required packages. Then, declare the state (using useCartContext and useState) and variables. Please note in the following code snippet that it has auth and product as props:

```js
import CartClient from "../api/CartClient";
import { updateCart, useCartContext } from "../hooks/CartContext";

const ProductCard = ({ auth, product }) => {
  const history = new useHistory();
  const cartClient = new CartClient(auth);
  const { cartItems, dispatch } = useCartContext();
  const [msg, setMsg] = new useState("");
  // continue…
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/ProductCard.js

먼저 장바구니에 제품을 추가하는 add() 비동기 함수를 작성합니다. 먼저 사용자가 로그인했는지 여부를 확인합니다. 그렇지 않은 경우 사용자를 로그인 페이지로 리디렉션합니다. checkLogin()은 useHistory 후크의 푸시 메서드를 사용하여 리디렉션합니다. auth의 토큰 속성은 사용자의 로그인 여부를 식별하는 데 사용됩니다.

사용자가 로그인한 것으로 확인되면 callAddItemApi 함수를 호출하여 장바구니에 제품을 추가합니다. callAddItemApi 함수는 먼저 장바구니에 제품이 있는지 여부를 찾습니다. 존재하는 경우 수량을 찾아 하나 더 추가합니다. 그런 다음 callAddItemApi 함수는 CartClient를 사용하여 REST API를 호출하여 새 항목을 추가하거나 기존 장바구니 항목의 수량을 업데이트합니다.

마지막에 add 함수는 dispatch를 호출하여 장바구니 컨텍스트에서 cartItems의 상태를 업데이트합니다.

```js
  // ProductCard.js continue…

  const add = async () => {
    const isLoggedIn = checkLogin();
    if (isLoggedIn && product?.id) {
      const res = await callAddItemApi();
      if (res && res.success) {
        if (res.data?.length > 0) {
          setMsg("Product added to bag.");
          dispatch(updateCart(res.data));
        }
      } else {
        setMsg(res && typeof res === "string" ? res : res.error.message);
      }
    }
  };

  const checkLogin = () => {
    if (!auth.token) {
      history.push("/login");
      return false;
    }
    return true;
  };

  const callAddItemApi = async () => {
    const qty = findQty(product.id);
    return cartClient.addOrUpdate({
      id: product.id, quantity: qty + 1, unitPrice:
          product.price
    });
  };

  const findQty = (id) => {
    const idx = cartItems.findIndex((i) => i.id === id);
    if (~idx) { return cartItems[idx].quantity; }
    return 0;
  };

  // continue…
```

The add function is called on a click of the Add to bag link. Similarly, the buy function shown in the following code snippet will be called when the user clicks on the Buy now button:
```js
  // ProductCard.js continue…

  const buy = async () => {
    const isLoggedIn = checkLogin();
    if (isLoggedIn && product?.id) {
      const res = await callAddItemApi();
      if (res && res.success) {
        history.push("/cart");
      } else {
        setMsg(res && typeof res === "string" ? res :
                                       res.error.message);
      }
    }
  };

  // continue…
```
This is very similar to the add function. However, on a successful response from callAddItemApi, it redirects the user to the cart page, using a useHistory hook instance.

Let's have a look at a JSX template. In the following code snippet, the className attribute values have been stripped for better readability:
```js
  // ProductCard.js continue…
  return (
    <div id={product.id} className="…">
      <figure className="…">
        <img src={product.imageUrl} alt={product.name}/>
        <div className="…">
          <form className="…">
            <div className="…">
              <h1 className="…">
                <Link to={`/products/${product.id}`}>
                {product.name}</Link>
              </h1>
              <div className="…">{"$"}{
                  product.price.toFixed(2)}
              </div>
              <div className="…">In stock</div>
            </div>
            <div className="…">
              <div className="…">
                <button className="…" type="button"
                    onClick={buy}
                >Buy now</button>
                <button className="…" type="button"
                   onClick={add}
                >Add to bag</button>
              </div>
            </div>
            <p className="…">Free shipping on all local
                orders.</p>
          </form>
        </div>
      </figure>
    </div>

  );

};

export default ProductCard;
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/ProductCard.js

The onClick event has been bound to buy and add for the Buy now button and the Add to bag link respectively. Also, the product name is a link created using Link. The to attribute of Link contains the path that points to the ProductDetail component. This path also contains the path parameter ID. You can use this parameter to perform certain operations on it. Similarly, you can also pass the query parameters the way you do in the browser URL.

When the user clicks on the product name, the user is redirected to the ProductDetail component (ProductDetail.js). Let's develop this next.

### Developing the ProductDetail component

The ProductDetail component is similar to the ProductCard component, except that it loads the product details from the backend by using the ID from the path.

Let's see how this is done. Only code related to the Fetch product has been shown in the following snippet. The rest of the code is the same as for the ProductCard component. However, you can refer to the full code in the GitHub repository:

```js
import { Link, useParams, useHistory } from "react-router-dom";
import ProductClient from "../api/ProductClient";
// Other imports removed for brevity
const ProductDetail = ({ auth }) => {
  const { id } = useParams();
  // Other declaration removed for brevity
  // Other functions removed for brevity

  useEffect(() => {
    async function getProduct(id) {
      const client = new ProductClient();
      const res = await client.fetch(id);
      if (res && res.success) {
        setProduct(res.data);
      }
    }
    // rest of code removed from brevity
    getProduct(id);
  }, [id]);
  return ( /* JSX Template */  );
};

export default ProductDetail;
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/ProductDetail.js

You have used useParams() from the react-router-dom package to retrieve the product ID passed from the ProductCard component. This id property is then used to fetch the product from the backend server using the ProductClient component. Upon a successful response, the retrieved product detail is set in the state product using the setProduct state function.

We are done with the development of product-based components such as ProductList, Products, ProductCard, and ProductDetail. We will now focus on authentication functionality so that we can later work on the cart and orders components, which require an authenticated user.


## 인증 구현

로그인 구성 요소 개발에 뛰어들기 전에 성공적인 로그인 응답에서 받은 토큰을 관리하는 방법과 액세스 토큰이 만료된 경우 인증이 필요한 호출을 만들기 전에 리프레시 토큰 요청을 시작해야 하는지 확인하는 방법을 알고 싶을 것입니다.

브라우저를 사용하면 쿠키, 세션 저장소 및 로컬 저장소에 토큰 또는 기타 정보를 저장할 수 있습니다. 서버 측에서 쿠키 또는 상태 저장 통신을 선택하지 않았으므로 나머지 두 가지 옵션이 남아 있습니다. 세션 저장소는 동일한 탭에 고유하고 새로 고침 버튼을 클릭하거나 탭을 닫는 즉시 지워지기 때문에 보다 안전한 응용 프로그램에 선호됩니다. 다른 탭과 페이지 새로 고침 간의 로그인 지속성을 관리하기를 원하므로 브라우저의 로컬 저장소를 선택합니다.

또한 장바구니 상태를 관리하는 것과 같은 방식으로 상태에 저장할 수도 있습니다. 그러나 이것은 세션 스토리지와 매우 유사합니다. 지금은 그 옵션을 남겨두자.

### useToken 후크 만들기

You have now used different React hooks. Let's move a step forward and create a custom hook. First, create a new hooks directory under the src directory, and create a useToken.js file in it.

```js
export default function useToken() {
  const getToken = () => {
   const tokenResponse = localStorage.getItem("tokenResponse");
   const userInfo = tokenResponse ? JSON.parse(tokenResponse) : "";
    return userInfo;
  };

  const [token, setToken] = useState(getToken());
  const saveToken = (tokenResponse) => {
    localStorage.setItem("tokenResponse", JSON.stringify(tokenResponse));
    setToken(tokenResponse);
  };
  return { setToken: saveToken, token };
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/hooks/useToken.js

여기에서 토큰 상태를 유지하기 위해 useState를 사용하고 있습니다. useState의 생성자에서 getToken 함수를 호출하여 토큰 상태를 선언하면서 토큰 상태를 초기화합니다.

이제 로그인 또는 로그아웃과 같은 작업에 변경 사항이 있을 때마다 초기 토큰 상태를 업데이트해야 하는 메커니즘을 제공해야 합니다. 이를 위해 saveToken이라는 새 함수를 만들 수 있습니다.

getToken 및 saveToken 함수는 모두 localStorage를 사용하여 토큰을 각각 검색하고 업데이트합니다. 마지막으로 토큰 상태와 saveToken 함수(setToken 형식)가 모두 사용에 대해 반환됩니다.

다음으로 인증을 위해 다른 REST API 클라이언트를 생성합니다. 다른 클라이언트인 Auth.js를 추가해 보겠습니다(https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/api/Auth .js), src/api 디렉토리 아래에 있습니다.

이 Auth.js 클라이언트는 다른 API 클라이언트와 매우 유사합니다. 여기에는 다음과 같이 설명된 백엔드 서버 REST API를 사용하여 로그인, 로그아웃 및 새로 고침 액세스 토큰 작업을 수행하는 세 가지 기능이 있습니다.

- login 작업은 App 구성 요소에서 전달한 상태 인수를 사용하여 로컬 저장소의 responseToken 키에 액세스 토큰, 새로 고침 토큰, 사용자 ID 및 사용자 이름을 설정합니다. App 구성 요소는 평소와 같이 useToken 사용자 지정 후크를 사용합니다. 로그인 작업은 또한 액세스 토큰의 만료 시간을 설정합니다.

- `refresh access token` 작업은 액세스 토큰과 유효 시간을 갱신한다.

- `logout` 동작은 토큰을 지우고 유효 시간을 0으로 설정한다.

You are done with the prerequisite work for implementing the login functionality and can now move on to creating the Login component.

### Writing the Login component

Let's create a new Login.js file under the src/components directory and then run the following code:

```js
Login.propTypes = {
  auth: PropTypes.object.isRequired,
};
const Login = ({ uri, auth }) => {
  const [username, setUserName] = useState();
  const [password, setPassword] = useState();
  const [errMsg, setErrMsg] = useState();
  const history = useHistory();
  
  const cancel = () => {
    const history_length = history.length;
    history_length > 2 ? history.goBack() : history.push("/");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await auth.loginUser({username, password});
    if (res && res.success) {
      setErrMsg(null);
      history.push(uri ? uri : "/");
    } else {
      setErrMsg(
        res && typeof res === "string" ? res : "Invalid Username/Password");
    }

  };
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Login.js

Before you start understanding the code, it's useful to know that PropTypes provides a way to check the type of passed props. Here, we have made sure that the auth prop is an object and a required prop. You may see messages in the console if it fails. Normally, you add this props check at the end of a file (in source code, it is at the bottom), but here it has been added at the top for better readability.

This component contains two props: auth and uri. The auth prop represents the authentication client, and uri is a string that sends the user back to the appropriate page after a successful login.

It has two functions: handleSubmit and cancel. The cancel function just sends back the user back to the previous page or the home page. The handleSubmit function makes use of the authentication client and calls the login API with the username and password.

Let's have a look at its JSX template, as follows:

```js
  return (

    <div className="…"><div className="…">

      <div className="…" role="dialog" aria-modal="true">

      <div className="…"><div className="…">

      <div className="…"><div className="…">

      <h2 className="…">Sign in to your account</h2>

      <form className="…" onSubmit={handleSubmit}>

      <div className="…"><div>

      <span className="…" style={{ dispay: errMsg ? "block" :

                                "none" }} >{errMsg}</span>

      <label htmlFor="username" className="…">Username

      </label>

      <input id="username" name="username" type="username"

       autoComplete="username" placeholder="Username"

           required

       className="…" onChange={(e) =>
           setUserName(e.target.value)}/>

      </div><div>

      <label htmlFor="password" className="…">Password
      </label>

      <input id="password" name="password" type="password"  

       autoComplete="password" placeholder="Password" required
       className="…" onChange={(e) => setPassword(e.target. value)}/>

      </div></div><div className="…"><div>
      <button type="submit" className="…">

       <span className="…">

       <svg className="…" xmlns=http://www.w3.org/2000/svg
        viewBox="0 0 20 20" fill="currentColor" aria-
        hidden="true"><path fillRule="evenodd" d="M5 9V7a5 5 0 0110

        0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-

        2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd"/>

       </svg>

       </span><span className="…">Sign in</span>

      </button>

      </div><div className="…">
      <button type="button" onClick={cancel}
      >Cancel</button>
      </div></div>
      </form>
      </div></div></div></div></div></div></div>
  );};

export default Login;
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Login.js

The handleSubmit function is called when a form is submitted (when the user clicks on the Sign in button). The cancel function is called when the user clicks on the Cancel button. Another noticeable point relates to setting the username and password states. These are set on onChange events respectively. The e.target.value argument represents the typed value in the respective input field. The e instance represents the event and target represents the target input field for the respective event.

So, now you know the complete flow: the user logs in and the app sets the required token and information in local storage. The API client uses this information to call the authenticated APIs. The logout operation, which is a part of the Header component (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Header.js), calls the Auth client's logout function, which calls the remove refresh token backend server's REST API and removes the authentication information from the local storage.

After authentication implementation, you need to write one more piece of code before you jump to writing the Cart component: cart context. Let's do that now.

### Writing the custom cart context

You can use the Redux library for centralizing and maintaining an application's global state. However, you would write a Redux-like custom hook to maintain the state for the cart. This uses createContext, useReducer, and useContext hooks from the React library.

You already know that createContext returns the Provider and Consumer. Therefore, when you create a CartContext using createContext, it would provide the CartContext.Provider. You won't use the Consumer, as you are going to use a useContext hook.

Next, you need a cart state (cartItems) that you pass to the value in CartContext.Provider so that it will be available in the component that use the CartContext. Now, we just need a reducer function. A reducer function accepts two arguments: state and action. Based on the provided action, it updates (mutates) the state and returns the updated state.

Now, let's jump into the code and see how it turns out. Have a look at the following snippet:

```js
export const CartContext = createContext();

function useCartContext() {
  return useContext(CartContext);
}

export const UPDATE_CART = "UPDATE_CART";
export const ADD_ITEM = "ADD_ITEM";
export const REMOVE_ITEM = "REMOVE_ITEM";

export function updateCart(items) {
  return { type: UPDATE_CART, items };
}

export function addItem(item) {
  return { type: ADD_ITEM, item };
}

export function removeItem(index) {
  return { type: REMOVE_ITEM, index };
}

export function cartReducer(state, action) {
  switch (action.type) {
    case UPDATE_CART:
      return [...action?.items];
    case ADD_ITEM:
      return [...state, action.item];
    case REMOVE_ITEM:
      const list = [...state];
      list.splice(action.index, 1);
      return list;
    default:
      return state;
  }
}

const CartContextProvider = (props) => {
  const [cartItems, dispatch] = useReducer(cartReducer,[]);
  const cartData = { cartItems, dispatch };
  return <CartContext.Provider value={cartData} {...props}
      />;
};

export { CartContextProvider, useCartContext };
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/hooks/CartContext.js

First, we have created a `CartContext` with a `createContext` hook. Then, we have declared a function that uses a useContext hook and returns the value field's value declared in the `CartContext.Provider` tag.

Next, you need a reducer function that uses the action and state. Therefore, we first define action types such as UPDATE_CART and then write functions that return an action object that contains both action type and argument value, such as updateCart. Finally, you can write a reducer function that takes state and action as arguments and, based on the passed action type, it mutates the state and returns the updated state.

Next, you define a CartContextProvider function that returns the `CartContext.Provider` component. Here, you use the reducer function in useReducer hook, and in its second argument, you pass the empty array as an initial state. The useReducer hook returns to the state and dispatch functions. The dispatch function takes the action object as an argument. You can use the function that returns the action object, such updateCart and addItem. You wrap the state (cartItems) and dispatcher functions (dispatch) in the cartData object and pass it to the value attribute in the CartContext.Provider component. At the end, it exports both the CartContextProvider and useCartContext functions.

You are going to use CartContextProvider as a component wrapper in the App component. This makes cartData (cartItems and dispatch) available to all components inside CartContextProvider, which can be accessed and used using useCartContext.

Now, finally, you can write the Cart component in the next subsection.

### Writing the Cart component

The Cart component is a parent component because it can have multiple items (CartItem component) in it. Let's create a new cart.js file in the src/components directory and add the following code to it:

```js
import CartClient from "../api/CartClient";
import CustomerClient from "../api/CustomerClient";
import OrderClient from "../api/OrderClient";
import { removeItem, updateCart, useCartContext } from "../hooks/CartContext";
import CartItem from "./CartItem";

const Cart = ({ auth }) => {
  const [grandTotal, setGrandTotal] = useState(0)
  const [noRecMsg, setNoRecMsg] = useState("Loading...");
  const history = useHistory();
  const cartClient = new CartClient(auth);
  const orderClient = new OrderClient(auth);
  const customerClient = new CustomerClient(auth);
  const { cartItems, dispatch } = useCartContext();

  // Continue…
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Cart.js

You use useCartContext here and import functions such as updateCart that return the action object (consumed by the dispatch function). Apart from CartClient, you also use OrderClient and CustomerClient here for checkout operations.

Let's add functions for calculating the total (calTotal) and increasing the quantity (increaseQty) of a given product ID, as shown next:

```js
  // Cart.js Continue…

  const calTotal = (items) => {
    let total = 0;
    items?.forEach((i) => (total = total + i?.unitPrice * i?.quantity));

    return total.toFixed(2);
  };

  const increaseQty = async (id) => {
    const idx = cartItems.findIndex((i) => i.id === id);
    if (~idx) {
      cartItems[idx].quantity = cartItems[idx].quantity + 1;
      const res = await cartClient.addOrUpdate(cartItems[idx]);
      if (res && res.success) {
        refreshCart(res.data);
        if (res.data?.length < 1) { setNoRecMsg("Cart is empty."); }

      } else {
        setNoRecMsg(res && typeof res === "string" ? res : res.error.message);
      }
    }
  }; // Continue…
```
The increaseQty function first finds whether the given ID exists in cart items or not. If it exists, then it increases the quantity of a product by 1. Finally, it calls the REST API to update the cart items and uses the response to update the cart by calling the refreshCart function.

Let's add a decreaseQty function, which is similar to increaseQty but decreases the quantity by one. Also, the deleteItem function would remove the cart item from the cart. The code is shown in the following snippet:
```js
  // Cart.js Continue…

  const decreaseQty = async (id) => {
    const idx = cartItems.findIndex((i) => i.id === id);
    if (~idx && cartItems[idx].quantity <= 1) {
      return deleteItem(id);
    } else if (cartItems[idx]?.quantity > 1) {
      cartItems[idx].quantity = cartItems[idx].quantity – 1;
      const res = await cartClient.addOrUpdate(cartItems[idx]);

      if (res && res.success) {
        refreshCart(res.data);
        if (res.data?.length < 1) { setNoRecMsg("Cart is empty.");}
        return;
      } else { 
        setNoRecMsg(res && typeof res === "string" ? res : res?.error?.message); 
      }
    }
  };

  const deleteItem = async (id) => {
    const idx = cartItems.findIndex((i) => i.id === id);
    if (~idx) {
      const res = await cartClient.remove(
          cartItems[idx].id);

      if (res && res.success) {
       dispatch(removeItem(idx));
       if (res.data?.length < 1) { setNoRecMsg("Item is removed.");}
      } else { setNoRecMsg(res && typeof res === "string" ? res
            : "There is an error performing the remove."); }
    }
  }; // Continue…
```
The decreaseQty function does one extra step in comparison to increaseQty— it removes the item if the existing quantity is 1 by calling the deleteItem. function.

The deleteItem function first finds the product based on a given ID. If it exists, then it calls the REST API to remove the product from the cart and updates the cart item state by calling the dispatch function with the action object returned by the removeItem function.

Let's define refreshCart and useEffect functions, as shown in the following code snippet:
```js
  // Cart.js Continue…
  const refreshCart = (items) => {
    setGrandTotal(calTotal(items));
    dispatch(updateCart(items));

  };

  useEffect(() => {
    async function fetch() {
      const res = await cartClient.fetch();
      if (res && res.success) {
        refreshCart(res.data.items);
        if (res.data?.items && res.data.items?.length < 1) {
          setNoRecMsg("Cart is empty.");
        }
      } else {
        setNoRecMsg(res && typeof res === "string" ? res                               : res.error.message);
      }
    }
    fetch();
  }, []);// Continue…
```

The refreshCart function updates the total and dispatches the updateCart action. The useEffect loads the cart items from the backend server and calls refreshCart to update the cartItems global state.

Let's add the last function of the Cart component to perform the checkout operation, as shown in the following code snippet:
```js
  // Cart.js Continue…

  const checkout = async () => {

    const res = await customerClient.fetch();

    if (res && res.success) {

      const payload = {

        address: { id: res.data.addressId },

        card: { id: res.data.cardId },

      };

      const orderRes = await orderClient.add(payload);

      if (orderRes && orderRes.success) {

        history.push("/orders");

      } else {

        setNoRecMsg(orderRes && typeof orderRes ===

            "string"

            ? orderRes: "Couldn't process checkout."

        );

      }

    } else {

      setNoRecMsg( res && typeof res === "string" ? res                                 : "error retreiving customer");

    }

  };
```
The checkout function first fetches the customer information and forms a payload for placing the order. On a successful POST order API response, the user is redirected to the Orders component.

Finally, let's add a JSX template, which is used from codepen user abdelrhman for the Cart component as shown in the next code block (Code and className values have been stripped for brevity):
```js
  // Cart.js Continue…

  return (

    <div className="…">

      <!-- code stripped for brevity  -->

          <div className="…">

            <h1 className="…">Shopping Cart</h1>

            <h2 className="…">{cartItems?.length}

            Items</h2>

          </div>

          <div className="…">

            <h3 className="…">Product Details</h3>

            <h3 className="…">Quantity</h3>

            <h3 className="…">Price</h3>

            <h3 className="…">Total</h3>

          </div>

          {cartItems && cartItems.length > 0 ? (

            cartItems?.map((i) => (

             <CartItem item={i} key={i.id}

                 removeItem={deleteItem}

              increaseQty={

                  increaseQty}decreaseQty={decreaseQty}/>

        <!-- code stripped for brevity  -->

          <div className="…">

            <div className="…">

              <span>Total cost</span>

              <span>${grandTotal}</span>

            </div>

            <button className="…" onClick={checkout}

              disabled={grandTotal == 0 ? true : false} >

              Checkout

            </button>

        <!-- code stripped for brevity  -->

    </div>

  );

};

export default Cart;
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Cart.js

Here, you can see that on a click of the Checkout button, it calls the checkout function to place the user order. Cart items are rendered using the CartItem component that you create next. You pass the removeItem, increaseQty, and decreaseQty functions as props to it.

Let's write the CartItem component by creating a new file (src/components/CartItem.js) and adding the following code:
```
import { useEffect, useState } from "react";

import { Link } from "react-router-dom";

const CartItem = ({ item, increaseQty, decreaseQty, removeItem }) => { const des = item ? item.description?.split(".") : [];

  const author = des && des.length > 0       ? des[des.length - 1] : "";

  const [total, setTotal] = useState();

  const calTotal = (item) => {

    setTotal((item?.unitPrice *               item?.quantity)?.toFixed(2));

  };

  const updateQty = (qty) => {

    if (qty === -1) { decreaseQty(item?.id); }

    else if (qty === 1) { increaseQty(item?.id); }

    else { return false; }

    calTotal(item);

  };

  useEffect(() => {

    calTotal(item);

  }, []);
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/CartItem.js

Here, you maintain the state of the total that is a product of the quantity and the unit price (calTotal function) and the updateQty helper function to perform the increase/decrease quantity operations. The useEffect hook also calls calTotal to update the total on the Cart page.

Let's add the last piece of the JSX template for the CartItem component, as shown in the next code block (className values have been stripped for brevity):
```js
  return (

    <div className="…">

      <div className="…">

        <div className="…">

          <img className="…" src={item?.imageUrl} alt="" />

        </div>

        <div className="…">

          <Link to={"/products/" + item.id} className="…">

            {item?.name}

          </Link>

          <span className="…">Author: {author}</span>

          <button className="…" onClick={() =>

              removeItem(item.id)}>

            Remove

          </button>

        </div>

      </div>

      <div className="…">

        <span className="…" onClick={() => updateQty(-1)}>

          <svg className="…" viewBox="0 0 448 512">

            <path d="M416 208H32c-17.67 0-32 14.33-32 32v32c0             17.67              14.33 32 32 32h384c17.67 0 32-14.33             32-32v-32c0-17.67-            14.33-32-32-32z" /></svg>

        </span>

        <input type="text" readOnly value={item?.quantity} />

        <span className="…" onClick={() => updateQty(1)}>

          <svg className="…" viewBox="0 0 448 512">

            <path d="M416 208H272V64c0-17.67-14.33-32-32-32h-            32c-            17.67 0-32 14.33-32 32v144H32c-17.67 0-32 14.33-32             32v32c0 17.67 14.33 32 32 32h144v144c0 17.67 14.33             32 32             32h32c17.67 0 32-14.33 32-32V304h144c17.67 0 32-            14.33             32-32v-32c0-17.67-14.33-32-32-32z" /></svg>

        </span>

      </div>

      <span className="…">{item?.unitPrice?.toFixed(2)}</span>

      <span className="…">${total}</span>

    </div>

  );

};

export default CartItem;
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/CartItem.js

The state item is used in an expression to generate the values. The removeItem and updateQty functions are bound to the onClick event for the respective JSX elements.

Now, you can write the last component (page) of this application in the next subsection: the Order Component.

### Writing the Order component

The Order component contains the order details fetched from the backend server. It shows date, status, amount, and items in a tabular format. It loads the order details on the first render with the useEffect hook and then the orders state is used in the JSX expression to display it.

Let's create a new file, Orders.js, in the src/components directory and add the following code to it:
```js
import OrderClient from "../api/OrderClient";

const Orders = ({ auth }) => {
  const [orders, setOrders] = useState([]);
  const formatDate = (dt) => {
    return dt && new Date(dt).toLocaleString();
  };

  useEffect(() => {
    async function fetchOrders() {
      const client = new OrderClient(auth);
      const res = await client.fetch();
      if (res && res.success) {
        setOrders(res.data);
      }
    }
    fetchOrders();
  }, []);
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Orders.js

Next, add the following JSX template. Here, the code and className have been stripped for brevity:
```js
  return (
    <div className="…">
      <!-- code stripped for brevity  -->
              <table className="…">
                <thead className="…">
                  <tr>
                    <th scope="col" className="…">Order Date</th>
                    <th scope="col" className="…">Order Items</th>
                    <th scope="col" className="…">Status</th>
                    <th scope="col" className="…">Order Amount</th>
                  </tr>
                </thead>

                <tbody className="…">{orders && orders.length < 1 ?

                  ( <tr className="px-6 py-4 whitespace-nowrap">
                     Found zero order</tr>
                  ) : (
                    orders?.map((order) => (
                  <tr>
                    <td className="…">
                      <div className="…">{formatDate(order?.date)}
                      </div>
                      <div className="…">Local Time</div>
                    </td>

                    <td className="…">
                       <!-- code stripped for brevity  -->
                            {order?.items.map((o, idx) => (
                              <div>
                                <span className="…">
                                  {idx + 1}.
                                </span>{" "}{o.name}{" "}
                                <span className="…">

                                  ({o?.quantity +" x $" +
                                  o?.unitPrice?.toFixed(2)}

                                  )

                                </span><br/>

                              </div>

                            ))}

                           </div>

                        </div>

                    </div>

                  </td>

                  <td className="…"><span className="…">{

                      order?.status}</span>

                  </td>

                  <td className="…">${

                      order?.total?.toFixed(2)}</td>

                      </tr>

                    ))

                  )}

                </tbody>

              </table>

        <!-- code stripped for brevity  -->

      </div>

    </div>

  );

};

export default Orders;
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/components/Orders.js

It simply displays the information fetched from the orders state.

Now, we can update the root component to complete the flow and test the application after starting again with the yarn start command.

### Writing the root (App) 

The App component is a root component of the React application. It contains routing information and the application layout with all the parent components, such as the product list and orders components.

Update the App.js file available in the project src directory with the following code:
```js

import Header from "./components/Header";
import Footer from "./components/Footer";
import ProductList from "./components/ProductList";
import Login from "./components/Login";
import useToken from "./hooks/useToken";
import Cart from "./components/Cart";
import ProductDetail from "./components/ProductDetail";
import NotFound from "./components/NotFound";
import Auth from "./api/Auth";
import { CartContextProvider } from "./hooks/CartContext";
import Orders from "./components/Orders";

function App() {
  const { token, setToken } = useToken();
  const auth = new Auth(token, setToken);
  const loginComponent = (props) => (
    <Login {...props} uri="/login" auth={auth} />
  );

  const productListComponent= (props)=> <ProductList auth={auth}/>;

  // continue…
```
This contains all the imports required for the App component. Then, you use the useToken() hook and the Auth authentication REST API client for authentication purposes. You create functions that return loginComponent and productListComponent.

Its JSX template is different from what we have used till now. It uses the BrowserRouter (Router), Route, and Switch components from the react-router-dom package. You define all the Route components inside the BrowserRouter component. Here, we are also using the Switch component because we want to render components exclusively. It also allows you to render the NotFound component (the typical 404 – not found page) if no path matches. The Route component allows you to define the path and component to be rendered. You have used the arrow function as a render property value because we can then use the expressions too. The following code snippet contains the logic explained here:
```js
  // App.js continue…

  return (

   <div className="flex flex-col min-h-screen h-full ">
    <Router>
     <Header userInfo={token} auth={auth} />
      <div className="flex-grow flex-shrink-0 p-4">
       <CartContextProvider>
         <Switch>
           <Route path="/" exact render={() => productListComponent()} />
           <Route path="/login" render={(props) => token ?
             productListComponent() : loginComponent(props)} />
           <Route path="/cart" render={(props) => token ?
             <Cart auth={auth} /> : loginComponent(props)} />
           <Route path="/orders" render={(props) => token ?
             <Orders auth={auth} /> : loginComponent(props)} />
           <Route path="/products/:id" render={() =>
             <ProductDetail auth={auth} />} />
           <Route path="*" exact component={NotFound} />
         </Switch>
       </CartContextProvider>
      </div>
     <Footer />
    </Router>
   </div>
  );
}

export default App;
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter07/ecomm-ui/src/App.js

All components are wrapped inside CartContextProvider to allow cartItems and dispatch to be accessible in all components provided they use the useCartContext custom hook.

## Running the application

You can start the backend server by using code from Chapter 6, Security (Authorization and Authentication). Then, you can start the ecomm-ui app by executing a yarn start command from the project root directory. You can log in with scott/tiger and perform all the operations.

## Summary

In this chapter, you have learned React basic concepts and created different types of components using them. You have also learned how to use the browser's built-in Fetch API to consume the REST APIs. You acquired the following skills in React: developing a component-based UI, implementing routing, consuming REST APIs, implementing functional components with hooks, writing custom hooks, and building a global state store with a React context API and a useReducer hook. The concepts and skills you acquired in this chapter lay a solid foundation for modern frontend development and give you an edge to gain the perspective of 360-degree application development.

In the next chapter, you will learn about writing tests for REST-based web services.

## Questions

1. What is the difference between props and state?
What is an event and how you can bind events in a React component?

2. What is a higher-order component?

## Further reading

1. Mastering React Test Driven Development:
https://www.packtpub.com/product/mastering-react-test-driven-development/9781789133417

2. React documentation:
https://reactjs.org/docs/

3. React Router guide:
https://reactrouter.com/web/guides/quick-start